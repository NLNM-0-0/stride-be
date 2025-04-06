package com.stride.tracking.identityservice.utils.mail;

import java.util.Map;

public class SendOTPMail implements MailFormatGenerator {
    private static final String SUBJECT = "Your OTP Code";

    @Override
    public String getSubject() {
        return SUBJECT;
    }

    /**
     * Generates an email template for sending OTP.
     * @param properties: Must provide "name" and "otp".
     */
    @Override
    public String generate(Map<String, String> properties) {
        String name = properties.getOrDefault("name", "User");
        String otp = properties.getOrDefault("otp", "XXXXXX");

        return String.format(
                "<html>" +
                        "<body>" +
                        "<h3>Hi, %s!</h3>" +
                        "<p>Your OTP code is: <strong>%s</strong></p>" +
                        "<p>Please enter this code to proceed.</p>" +
                        "<p>Best regards,<br>Stride team</p>" +
                        "</body>" +
                        "</html>",
                name, otp
        );
    }
}
