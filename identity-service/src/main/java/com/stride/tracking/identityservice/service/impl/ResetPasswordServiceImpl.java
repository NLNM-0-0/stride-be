package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.event.SendEmailEvent;
import com.stride.tracking.dto.request.Recipient;
import com.stride.tracking.dto.request.ResetPasswordUserRequest;
import com.stride.tracking.dto.request.ResetPasswordUserSendOTPRequest;
import com.stride.tracking.dto.request.VerifyResetPasswordRequest;
import com.stride.tracking.dto.response.VerifyResetPasswordResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl  implements ResetPasswordService {
    private final UserIdentityRepository userIdentityRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OTPGenerator otpGenerator;
    private final PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${password-reset-verify-token.duration}")
    protected long RESET_PASSWORD_VERIFY_DURATION;

    @Override
    public void sendOTPResetPassword(ResetPasswordUserSendOTPRequest request) {
        UserIdentity userIdentity = findExistingUserIdentityByUsername(request.getUsername());

        String newOTP = otpGenerator.generateOTP();

        refreshResetPasswordToken(userIdentity, newOTP);

        sendResetPasswordEmail(userIdentity, newOTP);
    }

    private void refreshResetPasswordToken(UserIdentity userIdentity, String newOTP) {
        ResetPasswordToken resetPasswordToken;

        Optional<ResetPasswordToken> resetPasswordTokenOptional
                = resetPasswordTokenRepository.findByUserIdentity_Id(userIdentity.getId());
        if (resetPasswordTokenOptional.isPresent()) {
            resetPasswordToken = resetPasswordTokenOptional.get();
            if (resetPasswordToken.getRetry() > ResetPasswordToken.MAX_RETRY) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_EXCEED_MAX_RETRY);
            }

            resetPasswordToken.setToken(newOTP);
            resetPasswordToken.setRetry(resetPasswordToken.getRetry() + 1);
            resetPasswordToken.setExpiryTime(Date.from(Instant.now().plus(RESET_PASSWORD_VERIFY_DURATION, ChronoUnit.SECONDS)));
        } else {
            resetPasswordToken = ResetPasswordToken.builder()
                    .expiryTime(Date.from(Instant.now().plus(RESET_PASSWORD_VERIFY_DURATION, ChronoUnit.SECONDS)))
                    .retry(1)
                    .token(newOTP)
                    .userIdentity(userIdentity)
                    .build();
        }
        resetPasswordTokenRepository.save(resetPasswordToken);
    }

    private UserIdentity findExistingUserIdentityByUsername(String username) {
        UserIdentity userIdentity = userIdentityRepository.findByUsername(username)
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST));

        if (userIdentity.isBlocked()) {
            throw new StrideException(HttpStatus.FORBIDDEN, Message.USER_IS_BLOCKED);
        }

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

        kafkaTemplate.send(KafkaTopics.NOTIFICATION_TOPIC, notificationEvent);
    }

    @Override
    public VerifyResetPasswordResponse verifyResetPassword(VerifyResetPasswordRequest request) {
        UserIdentity userIdentity = findExistingUserIdentityByUsername(request.getUsername());

        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByUserIdentity_Id(userIdentity.getId())
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_NOT_EXIST));

        checkResetPasswordTokenValid(resetPasswordToken, resetPasswordToken.getToken());

        return VerifyResetPasswordResponse.builder()
                .resetPasswordId(resetPasswordToken.getId())
                .build();
    }

    private void checkResetPasswordTokenValid(ResetPasswordToken resetPasswordToken, String token) {
        if (resetPasswordToken.getExpiryTime().before(new Date())) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_EXPIRED);
        }
        if (!resetPasswordToken.getToken().equals(token)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_NOT_CORRECT);
        }
    }

    @Override
    public void resetPassword(String resetPasswordTokenId, ResetPasswordUserRequest request) {
        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findById(resetPasswordTokenId)
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.RESET_PASSWORD_TOKEN_NOT_EXIST));

        checkResetPasswordTokenValid(resetPasswordToken, resetPasswordToken.getToken());

        UserIdentity userIdentity = resetPasswordToken.getUserIdentity();
        userIdentity.setPassword(passwordEncoder.encode(request.getPassword()));
        userIdentityRepository.save(userIdentity);

        resetPasswordTokenRepository.delete(resetPasswordToken);
    }
}
