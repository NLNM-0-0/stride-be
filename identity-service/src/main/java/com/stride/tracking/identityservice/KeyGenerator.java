package com.stride.tracking.identityservice;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class KeyGenerator {
    private static final String SECRET_KEY = "Stride123";

    public static void main(String[] args) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedKey = digest.digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        String signerKey = Base64.getEncoder().encodeToString(hashedKey);
        log.info("Signing key: {}", signerKey);
    }
}
