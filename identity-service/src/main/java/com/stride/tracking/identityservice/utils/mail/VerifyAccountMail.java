package com.stride.tracking.identityservice.utils.mail;

import java.util.Map;

public class VerifyAccountMail implements MailFormatGenerator {
    private static final String SUBJECT = "Verify Your Stride Account";

    @Override
    public String getSubject() {
        return SUBJECT;
    }

    /**
     * Generates an email template for account verification.
     * @param properties: Must provide "name" and "otp".
     */
    @Override
    public String generate(Map<String, String> properties) {
        String name = properties.getOrDefault("name", "User");
        String otp = properties.getOrDefault("otp", "XXXXXX");

        return String.format(
                "<html>" +
                        "<body>" +
                        "<p>Hi, <strong>%s!</strong>" +
                        "<p>Congratulations, you have successfully registered for a Stride account.</p>" +
                        "<p>Your OTP is: <strong>%s</strong></p>" +
                        "<p>Please enter this code to verify your account.</p>" +
                        "<p>Best regards,<br>Stride Team</p>" +
                        "</body>" +
                        "</html>",
                name, otp
        );
    }
}
