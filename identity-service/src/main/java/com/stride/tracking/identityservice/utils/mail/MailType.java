package com.stride.tracking.identityservice.utils.mail;

public enum MailType {
    VERIFY_ACCOUNT(new VerifyAccountMail()),
    SEND_OTP(new SendOTPMail()),
    RESET_PASSWORD_USER(new ResetPasswordUserMail()),
    RESET_PASSWORD_ADMIN(new ResetPasswordAdminMail())
    ;

    public final MailFormatGenerator generator;

    MailType(MailFormatGenerator generator) {
        this.generator = generator;
    }
}
