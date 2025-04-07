package com.stride.tracking.identityservice.service.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.request.*;
import com.stride.tracking.dto.response.AuthenticationResponse;
import com.stride.tracking.dto.response.CreateUserResponse;
import com.stride.tracking.dto.response.IntrospectResponse;
import com.stride.tracking.identityservice.client.ProfileFeignClient;
import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.constant.JWTClaimProperty;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.model.AuthToken;
import com.stride.tracking.identityservice.model.UserIdentity;
import com.stride.tracking.identityservice.repository.AuthTokenRepository;
import com.stride.tracking.identityservice.repository.UserIdentityRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
import com.stride.tracking.identityservice.service.AuthenticationService;
import com.stride.tracking.identityservice.utils.GoogleIdTokenHelper;
import com.stride.tracking.identityservice.utils.JwtTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserIdentityRepository userIdentityRepository;
    private final AuthTokenRepository authTokenRepository;

    private final ProfileFeignClient profileClient;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenHelper jwtTokenHelper;
    private final GoogleIdTokenHelper googleIdTokenHelper;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @Transactional
    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();

        try {
            SignedJWT signedJWT = jwtTokenHelper.verifyToken(token);

            if (authTokenRepository.existsByToken(signedJWT.getJWTClaimsSet().getJWTID())) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_LOGIN);
            }

            Map<String, Object> claims = signedJWT.getJWTClaimsSet().getClaims();

            String username = (String) claims.get(JWTClaimProperty.USERNAME);
            String userId = (String) claims.get(JWTClaimProperty.USER_ID);
            String email = (String) claims.get(JWTClaimProperty.EMAIL);
            String provider = (String) claims.get(JWTClaimProperty.PROVIDER);
            String scope = (String) claims.get(JWTClaimProperty.SCOPE);

            return IntrospectResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .username(username)
                    .email(email)
                    .provider(provider)
                    .scope(scope)
                    .build();
        } catch (StrideException | JOSEException | ParseException e) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }

    @Transactional
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        UserIdentity userIdentity = userIdentityRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST));

        boolean passwordMatched = passwordEncoder.matches(request.getPassword(), userIdentity.getPassword());
        if (!passwordMatched) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_CORRECT);
        }

        if (userIdentity.isBlocked()) {
            throw new StrideException(HttpStatus.FORBIDDEN, Message.USER_IS_BLOCKED);
        } else if (!userIdentity.isVerified()) {
            return AuthenticationResponse.builder()
                    .userIdentityId(userIdentity.getId())
                    .build();
        }

        return generateAndSaveAuthToken(userIdentity);
    }

    private AuthenticationResponse generateAndSaveAuthToken( UserIdentity userIdentity) {
        Date issueTime = new Date();
        Date expirationTime = Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS));
        String token = jwtTokenHelper.generateToken(userIdentity, issueTime, expirationTime);

        AuthToken authToken = AuthToken.builder()
                .token(token)
                .expiryTime(expirationTime)
                .build();
        authTokenRepository.save(authToken);

        return AuthenticationResponse.builder()
                .token(token)
                .expiryTime(expirationTime)
                .build();
    }

    @Transactional
    @Override
    public void logout(LogoutRequest request) {
        try {
            SignedJWT signedJWT = jwtTokenHelper.verifyToken(request.getToken());

            if (authTokenRepository.existsByToken(signedJWT.getJWTClaimsSet().getJWTID())) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_LOGIN);
            }

            AuthToken authToken = authTokenRepository
                    .findByToken(request.getToken())
                    .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.JWT_INVALID));
            authTokenRepository.delete(authToken);
        } catch (ParseException | JOSEException e) {
            log.error("Cannot parse logout token", e);
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.JWT_INVALID);
        } catch (Exception exception) {
            log.error("Cannot logout token", exception);
        }
    }

    @Transactional
    @Override
    public AuthenticationResponse authenticateWithGoogle(AuthenticateWithGoogleRequest request) {
        try {
            JWTClaimsSet claimsSet = googleIdTokenHelper.validateToken(request.getIdToken());

            String email = claimsSet.getStringClaim("email");
            String name = claimsSet.getStringClaim("name");
            String ava = claimsSet.getStringClaim("picture");
            String sub = claimsSet.getSubject();

            UserIdentity userIdentity = userIdentityRepository
                    .findByProviderAndEmail(AuthProvider.GOOGLE, email)
                    .orElseGet(() -> {
                        String userId = userIdentityRepository.findByUsername(email)
                                .map(UserIdentity::getUserId)
                                .orElseGet(() -> createUser(name, ava));

                        UserIdentity newUserIdentity = UserIdentity.builder()
                                .userId(userId)
                                .providerId(sub)
                                .provider(AuthProvider.GOOGLE)
                                .email(email)
                                .isBlocked(false)
                                .isVerified(true)
                                .isAdmin(false)
                                .build();

                        return userIdentityRepository.save(newUserIdentity);
                    });


            return generateAndSaveAuthToken(userIdentity);
        } catch (ParseException | JOSEException e) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.ID_TOKEN_INVALID);
        }
    }

    private String createUser(String name, String ava) {
        ResponseEntity<CreateUserResponse> response = profileClient.createUser(CreateUserRequest.builder()
                .name(name)
                .ava(ava)
                .build());
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.PROFILE_CREATE_USER_ERROR);
        }

        return Objects.requireNonNull(response.getBody()).getUserId();
    }
}
