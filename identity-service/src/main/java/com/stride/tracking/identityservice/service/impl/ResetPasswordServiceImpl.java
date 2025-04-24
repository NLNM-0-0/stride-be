package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.email.event.SendEmailEvent;
import com.stride.tracking.dto.email.request.Recipient;
import com.stride.tracking.dto.register.request.VerifyResetPasswordRequest;
import com.stride.tracking.dto.resetpassword.request.ResetPasswordUserRequest;
import com.stride.tracking.dto.resetpassword.request.ResetPasswordUserSendOTPRequest;
import com.stride.tracking.dto.resetpassword.response.VerifyResetPasswordResponse;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.model.ResetPasswordToken;
import com.stride.tracking.identityservice.model.UserIdentity;
import com.stride.tracking.identityservice.repository.ResetPasswordTokenRepository;
import com.stride.tracking.identityservice.repository.UserIdentityRepository;
import com.stride.tracking.identityservice.service.ResetPasswordService;
import com.stride.tracking.identityservice.utils.OTPGenerator;
import com.stride.tracking.identityservice.utils.mail.MailFormatGenerator;
import com.stride.tracking.identityservice.utils.mail.MailType;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class ResetPasswordServiceImpl  implements ResetPasswordService {
    private final UserIdentityRepository userIdentityRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final KafkaProducer kafkaProducer;

    private final OTPGenerator otpGenerator;
    private final PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${password-reset-verify-token.duration}")
    protected long RESET_PASSWORD_VERIFY_DURATION;

    @Override
    public void sendOTPResetPassword(ResetPasswordUserSendOTPRequest request) {
        log.info("[sendOTPResetPassword] Requested by username: {}", request.getUsername());

        UserIdentity userIdentity = findExistingUserIdentityByUsername(request.getUsername());

        String newOTP = otpGenerator.generateOTP();

        log.debug("[sendOTPResetPassword] Generated OTP for {}: {}",  userIdentity.getUsername(), newOTP);

        refreshResetPasswordToken(userIdentity, newOTP);

        sendResetPasswordEmail(userIdentity, newOTP);

        log.info("[sendOTPResetPassword] OTP sent to email: {}", userIdentity.getEmail());
    }

    private void refreshResetPasswordToken(UserIdentity userIdentity, String newOTP) {
        ResetPasswordToken resetPasswordToken;

        Optional<ResetPasswordToken> resetPasswordTokenOptional
                = resetPasswordTokenRepository.findByUserIdentity_Id(userIdentity.getId());
        if (resetPasswordTokenOptional.isPresent()) {
            resetPasswordToken = resetPasswordTokenOptional.get();
            if (resetPasswordToken.getRetry() > ResetPasswordToken.MAX_RETRY) {
                log.error("[refreshResetPasswordToken] Retry limit exceeded for user: {}", userIdentity.getUsername());
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_EXCEED_MAX_RETRY);
            }

            resetPasswordToken.setToken(newOTP);
            resetPasswordToken.setRetry(resetPasswordToken.getRetry() + 1);
            resetPasswordToken.setExpiryTime(Date.from(Instant.now().plus(RESET_PASSWORD_VERIFY_DURATION, ChronoUnit.SECONDS)));

            log.info("[refreshResetPasswordToken] Token updated for user: {}, retry count: {}", userIdentity.getUsername(), resetPasswordToken.getRetry());
        } else {
            resetPasswordToken = ResetPasswordToken.builder()
                    .expiryTime(Date.from(Instant.now().plus(RESET_PASSWORD_VERIFY_DURATION, ChronoUnit.SECONDS)))
                    .retry(1)
                    .token(newOTP)
                    .userIdentity(userIdentity)
                    .build();

            log.info("[refreshResetPasswordToken] New token created for user: {}", userIdentity.getUsername());
        }
        resetPasswordTokenRepository.save(resetPasswordToken);
    }

    private UserIdentity findExistingUserIdentityByUsername(String username) {
        log.debug("[findExistingUserIdentityByUsername] Looking for username: {}", username);

        UserIdentity userIdentity = userIdentityRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[findExistingUserIdentityByUsername] User not found: {}", username);
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST);
                });

        if (userIdentity.isBlocked()) {
            log.error("[findExistingUserIdentityByUsername] User is blocked: {}", username);
            throw new StrideException(HttpStatus.FORBIDDEN, Message.USER_IS_BLOCKED);
        }

        log.debug("[findExistingUserIdentityByUsername] Found and valid user: {}", username);
        return userIdentity;
    }

    private void sendResetPasswordEmail(UserIdentity userIdentity, String otp) {
        MailFormatGenerator generator = MailType.RESET_PASSWORD_USER.generator;

        SendEmailEvent notificationEvent = SendEmailEvent.builder()
                .recipient(Recipient.builder()
                        .id(userIdentity.getUserId())
                        .email(userIdentity.getEmail())
                        .name(userIdentity.getUsername())
                        .build())
                .subject(generator.getSubject())
                .body(generator.generate(Map.of(
                        "name", userIdentity.getUsername(),
                        "otp", otp
                )))
                .build();

        kafkaProducer.send(KafkaTopics.NOTIFICATION_TOPIC, notificationEvent);
        log.info("[sendResetPasswordEmail] Email dispatched to Kafka for user: {}", userIdentity.getUsername());
    }

    @Override
    public VerifyResetPasswordResponse verifyResetPassword(VerifyResetPasswordRequest request) {
        log.info("[verifyResetPassword] Verifying OTP for username: {}", request.getUsername());

        UserIdentity userIdentity = findExistingUserIdentityByUsername(request.getUsername());

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByUserIdentity_Id(userIdentity.getId())
                .orElseThrow(() -> {
                    log.error("[verifyResetPassword] Token not found for user: {}", userIdentity.getUsername());
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_NOT_EXIST);
                });

        checkResetPasswordTokenValid(resetPasswordToken, resetPasswordToken.getToken());

        log.info("[verifyResetPassword] OTP verified successfully for user: {}", userIdentity.getUsername());

        return VerifyResetPasswordResponse.builder()
                .resetPasswordId(resetPasswordToken.getId())
                .build();
    }

    private void checkResetPasswordTokenValid(ResetPasswordToken resetPasswordToken, String token) {
        log.debug("[checkResetPasswordTokenValid] Validating reset password token for userIdentityId: {}",
                resetPasswordToken.getUserIdentity().getId());

        if (resetPasswordToken.getExpiryTime().before(new Date())) {
            log.error("[checkResetPasswordTokenValid] Token expired at: {}", resetPasswordToken.getExpiryTime());
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_EXPIRED);
        }
        if (!resetPasswordToken.getToken().equals(token)) {
            log.error("[checkResetPasswordTokenValid] Provided token is not correct. Expected: {}, Provided: {}",
                    resetPasswordToken.getToken(), token);
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_NOT_CORRECT);
        }

        log.debug("[checkResetPasswordTokenValid] Token is valid.");
    }

    @Override
    public void resetPassword(String resetPasswordTokenId, ResetPasswordUserRequest request) {
        log.info("[resetPassword] Attempting password reset with tokenId: {}", resetPasswordTokenId);

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findById(resetPasswordTokenId)
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_NOT_EXIST));

        checkResetPasswordTokenValid(resetPasswordToken, resetPasswordToken.getToken());

        UserIdentity userIdentity = resetPasswordToken.getUserIdentity();
        userIdentity.setPassword(passwordEncoder.encode(request.getPassword()));
        userIdentityRepository.save(userIdentity);

        log.debug("[resetPassword] Password updated for user: {}", userIdentity.getUsername());

        resetPasswordTokenRepository.delete(resetPasswordToken);

        log.info("[resetPassword] Token updated successfully for user: {}", userIdentity.getUsername());
    }
}
