package com.stride.tracking.identityservice.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.identityservice.constant.JWTClaimProperty;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.constant.Role;
import com.stride.tracking.identityservice.exception.AuthException;
import com.stride.tracking.identityservice.model.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenHelper {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    private final JWSAlgorithm jwsAlgorithm;

    public String generateToken(UserIdentity userIdentity, Date issueTime, Date expirationTime) {
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

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!verified || expiryTime.before(new Date())) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.JWT_INVALID);
        }

        return signedJWT;
    }
}
