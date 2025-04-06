package com.stride.tracking.identityservice.utils.mail;

import java.util.Map;

public class ResetPasswordUserMail implements MailFormatGenerator {
    private static final String SUBJECT = "Reset Your Password";

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
                        "<h3>Hello %s,</h3>" +
                        "<p>You requested a password reset. Use the OTP below to reset your password:</p>" +
                        "<strong>%s</strong>" +
                        "<p>If you did not request this, please ignore this email.</p>" +
                        "<p>Best regards,<br>Stride team</p>" +
                        "</body>" +
                        "</html>",
                name, otp
        );
    }
}