package com.stride.tracking.identityservice.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OTPGenerator {
    private final Random random = new Random();

    public String generateOTP() {
        int otp = 100_000 + random.nextInt(900_000);
        return String.valueOf(otp);
    }
}
