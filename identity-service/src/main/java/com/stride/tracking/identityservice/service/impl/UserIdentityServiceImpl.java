package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.event.SendEmailEvent;
import com.stride.tracking.dto.request.CreateUserRequest;
import com.stride.tracking.dto.request.EmailRegisterRequest;
import com.stride.tracking.dto.request.Recipient;
import com.stride.tracking.dto.request.VerifyAccountRequest;
import com.stride.tracking.dto.response.CreateUserResponse;
import com.stride.tracking.dto.response.EmailRegisterResponse;
import com.stride.tracking.identityservice.client.ProfileFeignClient;
import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.model.UserIdentity;
import com.stride.tracking.identityservice.model.VerifiedToken;
import com.stride.tracking.identityservice.repository.UserIdentityRepository;
import com.stride.tracking.identityservice.repository.VerifiedTokenRepository;
import com.stride.tracking.identityservice.service.UserIdentityService;
import com.stride.tracking.identityservice.utils.OTPGenerator;
import com.stride.tracking.identityservice.utils.mail.MailFormatGenerator;
import com.stride.tracking.identityservice.utils.mail.MailType;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {
    private final UserIdentityRepository userIdentityRepository;
    private final VerifiedTokenRepository verifiedTokenRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ProfileFeignClient profileClient;

    private final PasswordEncoder passwordEncoder;
    private final OTPGenerator otpGenerator;

    @NonFinal
    @Value("${verified-token.duration}")
    protected long VERIFIED_DURATION;

    @Transactional
    @Override
    public EmailRegisterResponse register(EmailRegisterRequest request) {
        String userIdentityId;

        Optional<UserIdentity> existingUserIdentityOptional =
                userIdentityRepository.findByUsername(request.getEmail());
        if (existingUserIdentityOptional.isPresent()) {
            UserIdentity existingUserIdentity = existingUserIdentityOptional.get();

            if (existingUserIdentity.isBlocked()) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_IS_BLOCKED);
            } else {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_EXISTED);
            }

        } else {
            Optional<UserIdentity> existingGoogleUser = userIdentityRepository.findByProviderAndEmail(
                    AuthProvider.GOOGLE, request.getEmail());

            String userId = existingGoogleUser
                    .map(UserIdentity::getUserId)
                    .orElseGet(() -> createUser(request));

            boolean isExistingUser = existingGoogleUser.isPresent();
            UserIdentity userIdentity = createUserIdentity(request, userId, isExistingUser);

            if (!isExistingUser) {
                String otp = saveVerifiedToken(userIdentity);
                sendVerifiedEmail(userIdentity, otp);
            }

            userIdentityId = userIdentity.getId();
        }

        return EmailRegisterResponse.builder()
                .userIdentityId(userIdentityId)
                .build();
    }

    private String createUser(EmailRegisterRequest request) {
        ResponseEntity<CreateUserResponse> response = profileClient.createUser(CreateUserRequest.builder()
                .name(request.getEmail())
                .build());
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.PROFILE_CREATE_USER_ERROR);
        }

        return Objects.requireNonNull(response.getBody()).getUserId();
    }

    private UserIdentity createUserIdentity(EmailRegisterRequest request, String userId, boolean isVerified) {
        UserIdentity userIdentity = UserIdentity.builder()
                .userId(userId)
                .email(request.getEmail())
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.STRIDE)
                .providerId(null)
                .isVerified(isVerified)
                .isAdmin(false)
                .isBlocked(false)
                .build();

        userIdentity = userIdentityRepository.save(userIdentity);

        return userIdentity;
    }

    private String saveVerifiedToken(UserIdentity userIdentity) {
        String otp = otpGenerator.generateOTP();

        VerifiedToken token = VerifiedToken.builder()
                .token(otp)
                .expiryTime(Date.from(Instant.now().plus(VERIFIED_DURATION, ChronoUnit.SECONDS)))
                .userIdentity(userIdentity)
                .retry(1)
                .build();

        verifiedTokenRepository.save(token);

        return otp;
    }

    private void sendVerifiedEmail(UserIdentity userIdentity, String otp) {
        MailFormatGenerator generator = MailType.VERIFY_ACCOUNT.generator;

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

    @Transactional
    @Override
    public void verifyAccount(String userId, VerifyAccountRequest request) {
        UserIdentity userIdentity = findExistingUserIdentityById(userId);
        VerifiedToken verifiedToken = findVerifiedTokenByUserIdentityId(userIdentity.getId());

        if (verifiedToken.getExpiryTime().before(new Date())) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_EXPIRED);
        }
        if (!verifiedToken.getToken().equals(request.getToken())) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_NOT_CORRECT);
        }

        userIdentity.setVerified(true);
        userIdentityRepository.save(userIdentity);
        verifiedTokenRepository.delete(verifiedToken);
    }

    private UserIdentity findExistingUserIdentityById(String id) {
        UserIdentity userIdentity = userIdentityRepository.findById(id)
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST));

        if (userIdentity.isBlocked()) {
            throw new StrideException(HttpStatus.FORBIDDEN, Message.USER_IS_BLOCKED);
        }

        return userIdentity;
    }

    private VerifiedToken findVerifiedTokenByUserIdentityId(String userIdentityId) {
        return verifiedTokenRepository.findByUserIdentity_Id(userIdentityId)
                .orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_NOT_EXIST));
    }

    @Transactional
    @Override
    public void sendVerifiedOTP(String userIdentityId) {
        UserIdentity userIdentity = findExistingUserIdentityById(userIdentityId);
        VerifiedToken verifiedToken = findVerifiedTokenByUserIdentityId(userIdentity.getId());

        if (verifiedToken.getRetry() > VerifiedToken.MAX_RETRY) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_EXCEED_MAX_RETRY);
        }

        String newOtp = otpGenerator.generateOTP();
        updateVerifiedTokenWithNewOtp(verifiedToken, newOtp);

        sendOTPEmail(userIdentity, newOtp);
    }

    private void updateVerifiedTokenWithNewOtp(VerifiedToken verifiedToken, String otp) {
        verifiedToken.setRetry(verifiedToken.getRetry() + 1);
        verifiedToken.setToken(otp);
        verifiedToken.setExpiryTime(Date.from(Instant.now().plus(VERIFIED_DURATION, ChronoUnit.SECONDS)));

        verifiedTokenRepository.save(verifiedToken);
    }

    private void sendOTPEmail(UserIdentity userIdentity, String otp) {
        MailFormatGenerator generator = MailType.SEND_OTP.generator;

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
}
