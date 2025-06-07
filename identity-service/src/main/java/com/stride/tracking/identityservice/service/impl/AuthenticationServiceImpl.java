package com.stride.tracking.identityservice.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.identity.dto.auth.request.AuthenticateWithGoogleRequest;
import com.stride.tracking.identity.dto.auth.request.AuthenticationRequest;
import com.stride.tracking.identity.dto.auth.request.IntrospectRequest;
import com.stride.tracking.identity.dto.auth.request.LogoutRequest;
import com.stride.tracking.identity.dto.auth.response.AuthenticationResponse;
import com.stride.tracking.identity.dto.auth.response.IntrospectResponse;
import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.constant.JWTClaimProperty;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.model.AuthToken;
import com.stride.tracking.identityservice.model.UserIdentity;
import com.stride.tracking.identityservice.repository.AuthTokenRepository;
import com.stride.tracking.identityservice.repository.UserIdentityRepository;
import com.stride.tracking.identityservice.service.AuthenticationService;
import com.stride.tracking.identityservice.utils.GoogleIdTokenHelper;
import com.stride.tracking.identityservice.utils.JwtTokenHelper;
import com.stride.tracking.profile.dto.profile.request.CreateProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserIdentityRepository userIdentityRepository;
    private final AuthTokenRepository authTokenRepository;

    private final ProfileService profileService;

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
        log.info("[introspect] Token received: {}", token);

        try {
            SignedJWT signedJWT = jwtTokenHelper.verifyToken(token);
            log.debug("[introspect] Token verified");

            if (authTokenRepository.existsByToken(signedJWT.getJWTClaimsSet().getJWTID())) {
                log.error("[introspect] Token has been revoked");
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_LOGIN);
            }

            Map<String, Object> claims = signedJWT.getJWTClaimsSet().getClaims();

            String username = (String) claims.get(JWTClaimProperty.USERNAME);
            String userId = (String) claims.get(JWTClaimProperty.USER_ID);
            String email = (String) claims.get(JWTClaimProperty.EMAIL);
            String provider = (String) claims.get(JWTClaimProperty.PROVIDER);
            String scope = (String) claims.get(JWTClaimProperty.SCOPE);

            log.info("[introspect] Valid token for user: {}", userId);

            return IntrospectResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .username(username)
                    .email(email)
                    .provider(provider)
                    .scope(scope)
                    .build();
        } catch (StrideException | JOSEException | ParseException e) {
            log.error("[introspect] Token introspection failed", e);
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }

    @Transactional
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("[authenticate] Attempting login for user: {}", request.getUsername());

        UserIdentity userIdentity = userIdentityRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("[authenticate] User not found: {}", request.getUsername());
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST);
                });

        boolean passwordMatched = passwordEncoder.matches(request.getPassword(), userIdentity.getPassword());
        if (!passwordMatched) {
            log.error("[authenticate] Incorrect password for user: {}", request.getUsername());
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_CORRECT);
        }

        if (userIdentity.isBlocked()) {
            log.error("[authenticate] User is blocked: {}", request.getUsername());
            throw new StrideException(HttpStatus.FORBIDDEN, Message.USER_IS_BLOCKED);
        } else if (!userIdentity.isVerified()) {
            log.error("[authenticate] User not verified: {}", request.getUsername());
            return AuthenticationResponse.builder()
                    .userIdentityId(userIdentity.getId())
                    .build();
        }

        log.info("[authenticate] User authenticated successfully: {}", request.getUsername());
        return generateAndSaveAuthToken(userIdentity);
    }

    private AuthenticationResponse generateAndSaveAuthToken(UserIdentity userIdentity) {
        Date issueTime = new Date();
        Date expirationTime = Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS));
        String token = jwtTokenHelper.generateToken(userIdentity, issueTime, expirationTime);

        AuthToken authToken = AuthToken.builder()
                .token(token)
                .userId(userIdentity.getUserId())
                .expiryTime(expirationTime)
                .build();
        authTokenRepository.save(authToken);

        log.debug("[generateAndSaveAuthToken] Generated token for userId: {}, expires at: {}", userIdentity.getUserId(), expirationTime);

        return AuthenticationResponse.builder()
                .token(token)
                .expiryTime(expirationTime)
                .build();
    }

    @Transactional
    @Override
    public void logout(LogoutRequest request) {
        log.info("[logout] Token to logout: {}", request.getToken());

        try {
            SignedJWT signedJWT = jwtTokenHelper.verifyToken(request.getToken());
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();

            if (authTokenRepository.existsByToken(jwtId)) {
                log.warn("[logout] Token already logged out: {}", jwtId);
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_LOGIN);
            }

            AuthToken authToken = authTokenRepository
                    .findByToken(request.getToken())
                    .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.JWT_INVALID));
            authTokenRepository.delete(authToken);

            log.info("[logout] Token successfully logged out: {}", jwtId);
        } catch (ParseException | JOSEException e) {
            log.error("[logout] Cannot parse logout token", e);
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.JWT_INVALID);
        } catch (Exception exception) {
            log.error("[logout] Unexpected error during logout", exception);
        }
    }

    @Transactional
    @Override
    public AuthenticationResponse authenticateWithGoogle(AuthenticateWithGoogleRequest request) {
        log.info("[authenticateWithGoogle] Authenticating with Google ID token");

        try {
            JWTClaimsSet claimsSet = googleIdTokenHelper.validateToken(request.getIdToken());

            String email = claimsSet.getStringClaim("email");
            String name = claimsSet.getStringClaim("name");
            String ava = claimsSet.getStringClaim("picture");
            String sub = claimsSet.getSubject();

            log.debug("[authenticateWithGoogle] Google account: {}, name: {}", email, name);

            UserIdentity userIdentity = userIdentityRepository
                    .findByProviderAndEmail(AuthProvider.GOOGLE, email)
                    .orElseGet(() -> createNewUserWithGoogleProvider(email, name, ava, sub));


            return generateAndSaveAuthToken(userIdentity);
        } catch (ParseException | JOSEException e) {
            log.error("[authenticateWithGoogle] Failed to validate Google ID token", e);
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.ID_TOKEN_INVALID);
        }
    }

    private UserIdentity createNewUserWithGoogleProvider(
            String email,
            String name,
            String ava,
            String sub
    ) {
        String userId = userIdentityRepository.findByUsername(email)
                .map(UserIdentity::getUserId)
                .orElseGet(() -> profileService.createProfile(
                                CreateProfileRequest.builder()
                                        .ava(ava)
                                        .name(name)
                                        .email(email)
                                        .build(),
                                AuthProvider.GOOGLE
                        )
                );

        UserIdentity newUserIdentity = UserIdentity.builder()
                .userId(userId)
                .providerId(sub)
                .provider(AuthProvider.GOOGLE)
                .email(email)
                .isBlocked(false)
                .isVerified(true)
                .isAdmin(false)
                .build();

        log.info("[createNewUserWithGoogleProvider] Creating new user in identity service for Google user: {}", email);

        return userIdentityRepository.save(newUserIdentity);
    }
}
