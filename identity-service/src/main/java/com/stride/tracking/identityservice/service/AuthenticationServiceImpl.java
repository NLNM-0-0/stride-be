package com.stride.tracking.identityservice.service;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.request.AuthenticationRequest;
import com.stride.tracking.dto.request.IntrospectRequest;
import com.stride.tracking.dto.request.LogoutRequest;
import com.stride.tracking.dto.response.AuthenticationResponse;
import com.stride.tracking.dto.response.IntrospectResponse;
import com.stride.tracking.identityservice.constant.JWTClaimProperty;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.constant.Role;
import com.stride.tracking.identityservice.exception.AuthException;
import com.stride.tracking.identityservice.model.AuthToken;
import com.stride.tracking.identityservice.model.UserIdentity;
import com.stride.tracking.identityservice.repository.AuthTokenRepository;
import com.stride.tracking.identityservice.repository.UserIdentityRepository;
import com.stride.tracking.identityservice.service.impl.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserIdentityRepository userIdentityRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWSAlgorithm jwsAlgorithm;


    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @Transactional
    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();

        try {
            SignedJWT signedJWT = verifyToken(token);

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

        Date issueTime = new Date();
        Date expirationTime = Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS));
        String token = generateToken(userIdentity, issueTime, expirationTime);

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

    private String generateToken(UserIdentity userIdentity, Date issueTime, Date expirationTime) {
        JWSHeader header = new JWSHeader(jwsAlgorithm);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userIdentity.getId())
                .issuer("stride")
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .jwtID(UUID.randomUUID().toString())
                .claim(JWTClaimProperty.SCOPE, userIdentity.isAdmin() ? Role.ADMIN : Role.USER)
                .claim(JWTClaimProperty.USERNAME, userIdentity.getUsername())
                .claim(JWTClaimProperty.USER_ID, userIdentity.getUserId())
                .claim(JWTClaimProperty.EMAIL, userIdentity.getEmail())
                .claim(JWTClaimProperty.PROVIDER, userIdentity.getProvider())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AuthException("Cannot create token", e);
        }
    }

    @Transactional
    @Override
    public void logout(LogoutRequest request) {
        try {
            verifyToken(request.getToken());

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

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!verified || expiryTime.before(new Date())) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.JWT_INVALID);
        }

        if (authTokenRepository.existsByToken(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_LOGIN);
        }

        return signedJWT;
    }
}
