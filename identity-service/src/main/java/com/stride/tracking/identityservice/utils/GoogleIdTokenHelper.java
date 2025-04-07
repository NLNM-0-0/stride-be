package com.stride.tracking.identityservice.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.stride.tracking.identityservice.exception.AuthException;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.List;

@Component
public class GoogleIdTokenHelper {
    @NonFinal
    @Value("${oauth2.google.public-keys-url}")
    protected String GOOGLE_PUBLIC_KEYS_URL;

    @NonFinal
    @Value("${oauth2.google.issuer}")
    protected String GOOGLE_ISSUER;

    @NonFinal
    @Value("${oauth2.google.client-id}")
    protected String GOOGLE_CLIENT_ID;

    public JWTClaimsSet validateToken(String idToken) throws JOSEException, ParseException {
        JWKSet jwkSet = getGooglePublicKeys();

        SignedJWT signedJWT = SignedJWT.parse(idToken);

        RSAKey rsaKey = getGoogleRSAKey(jwkSet, signedJWT.getHeader().getKeyID());

        RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
        if (!signedJWT.verify(verifier)) {
            throw new AuthException("Invalid ID token signature");
        }

        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        if (!claimsSet.getIssuer().equals(GOOGLE_ISSUER) ||
                !claimsSet.getAudience().contains(GOOGLE_CLIENT_ID)) {
            throw new AuthException("Invalid ID token claims");
        }

        return claimsSet;
    }

    private JWKSet getGooglePublicKeys() throws ParseException {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(GOOGLE_PUBLIC_KEYS_URL, String.class);
        if (response == null) {
            throw new AuthException("Can not get Google public keys");
        }
        return JWKSet.parse(response);
    }

    private RSAKey getGoogleRSAKey(JWKSet jwkSet, String keyId) {
        List<JWK> keys = jwkSet.getKeys();
        for (JWK key : keys) {
            if (key.getKeyID().equals(keyId)) {
                return (RSAKey) key;
            }
        }
        throw new AuthException("RSA key not found for key ID: " + keyId);
    }
}
