package com.stride.tracking.identityservice.utils.mail;

public class MailFormatGeneratorFactory {
    private MailFormatGeneratorFactory() {}

    public static MailFormatGenerator getGenerator(MailType mailType) {
        return switch (mailType) {
            case SEND_OTP -> new SendOTPMail();
            case VERIFY_ACCOUNT -> new VerifyAccountMail();
            case RESET_PASSWORD_USER -> new ResetPasswordUserMail();
            case RESET_PASSWORD_ADMIN -> new ResetPasswordAdminMail();
        };
    }
}
