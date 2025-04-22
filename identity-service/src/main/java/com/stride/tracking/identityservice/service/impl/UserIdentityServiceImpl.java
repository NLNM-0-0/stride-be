package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.dto.email.event.SendEmailEvent;
import com.stride.tracking.dto.email.request.Recipient;
import com.stride.tracking.dto.register.request.EmailRegisterRequest;
import com.stride.tracking.dto.register.request.VerifyAccountRequest;
import com.stride.tracking.dto.register.response.CreateUserResponse;
import com.stride.tracking.dto.register.response.EmailRegisterResponse;
import com.stride.tracking.dto.user.request.CreateUserRequest;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Log4j2
public class UserIdentityServiceImpl implements UserIdentityService {
    private final UserIdentityRepository userIdentityRepository;
    private final VerifiedTokenRepository verifiedTokenRepository;
    private final KafkaProducer kafkaProducer;

    private final ProfileFeignClient profileClient;

    private final PasswordEncoder passwordEncoder;
    private final OTPGenerator otpGenerator;

    @NonFinal
    @Value("${verified-token.duration}")
    protected long VERIFIED_DURATION;

    @Transactional
    @Override
    public EmailRegisterResponse register(EmailRegisterRequest request) {
        log.info("[register] Start register for email: {}", request.getEmail());

        String userIdentityId;

        Optional<UserIdentity> existingUserIdentityOptional =
                userIdentityRepository.findByUsername(request.getEmail());
        if (existingUserIdentityOptional.isPresent()) {
            UserIdentity existingUserIdentity = existingUserIdentityOptional.get();

            if (existingUserIdentity.isBlocked()) {
                log.error("[register] Blocked user attempted to register again: {}", request.getEmail());
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_IS_BLOCKED);
            } else {
                log.error("[register] User already exists: {}", request.getEmail());
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_EXISTED);
            }

        } else {
            Optional<UserIdentity> existingGoogleUser = userIdentityRepository.findByProviderAndEmail(
                    AuthProvider.GOOGLE, request.getEmail());

            String userId = existingGoogleUser
                    .map(UserIdentity::getUserId)
                    .orElseGet(() -> {
                        log.warn("[register] Can't find Google user in this system: {}", request.getEmail());
                        return createUser(request);
                    });

            boolean isExistingUser = existingGoogleUser.isPresent();
            UserIdentity userIdentity = createUserIdentity(request, userId, isExistingUser);

            if (!isExistingUser) {
                String otp = saveVerifiedToken(userIdentity);
                sendVerifiedEmail(userIdentity, otp);
                log.info("[register] Sent verification email to: {}", userIdentity.getEmail());
            }

            userIdentityId = userIdentity.getId();
        }

        log.info("[register] Register successful for email: {}", request.getEmail());
        return EmailRegisterResponse.builder()
                .userIdentityId(userIdentityId)
                .build();
    }

    private String createUser(EmailRegisterRequest request) {
        ResponseEntity<CreateUserResponse> response = profileClient.createUser(
                CreateUserRequest.builder()
                        .name(request.getEmail())
                        .build()
        );
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            log.error("[createUser] Failed to create user profile for email: {}", request.getEmail());
            throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.PROFILE_CREATE_USER_ERROR);
        }

        log.debug("[createUser] Created user profile with userId: {}", response.getBody().getUserId());
        return Objects.requireNonNull(response.getBody()).getUserId();
    }

    private UserIdentity createUserIdentity(EmailRegisterRequest request, String userId, boolean isVerified) {
        log.debug("[createUserIdentity] Creating new user identity for email: {}, userId: {}, isVerified: {}",
                request.getEmail(), userId, isVerified);

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

        log.debug("[createUserIdentity] User identity created and saved for userId: {}", userIdentity.getUserId());

        return userIdentity;
    }

    private String saveVerifiedToken(UserIdentity userIdentity) {
        String otp = otpGenerator.generateOTP();

        log.debug("[saveVerifiedToken] Generated OTP: {} for user: {}", otp, userIdentity.getUsername());

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
        log.info("[sendVerifiedEmail] Sending verification email to user: {}", userIdentity.getEmail());

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

        kafkaProducer.send(KafkaTopics.NOTIFICATION_TOPIC, notificationEvent);

        log.info("[sendVerifiedEmail] Verification email sent to: {}", userIdentity.getEmail());
    }

    @Transactional
    @Override
    public void verifyAccount(String userId, VerifyAccountRequest request) {
        log.info("[verifyAccount] Verifying account for userId: {}", userId);

        UserIdentity userIdentity = findExistingUserIdentityById(userId);
        VerifiedToken verifiedToken = findVerifiedTokenByUserIdentityId(userIdentity.getId());

        if (verifiedToken.getExpiryTime().before(new Date())) {
            log.error("[verifyAccount] Verified token expired for userId: {}", userId);
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_EXPIRED);
        }
        if (!verifiedToken.getToken().equals(request.getToken())) {
            log.error("[verifyAccount] Incorrect verified token for userId: {}", userId);
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_NOT_CORRECT);
        }

        userIdentity.setVerified(true);
        userIdentityRepository.save(userIdentity);
        verifiedTokenRepository.delete(verifiedToken);

        log.info("[verifyAccount] Account successfully verified for userId: {}", userId);
    }

    private UserIdentity findExistingUserIdentityById(String id) {
        log.debug("[findExistingUserIdentityById] Checking user existence for userId: {}", id);

        UserIdentity userIdentity = userIdentityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[findExistingUserIdentityById] User not found for userId: {}", id);
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST);
                });

        if (userIdentity.isBlocked()) {
            log.error("[findExistingUserIdentityById] User is blocked for userId: {}", id);
            throw new StrideException(HttpStatus.FORBIDDEN, Message.USER_IS_BLOCKED);
        }

        log.debug("[findExistingUserIdentityById] User found for userId: {}", id);
        return userIdentity;
    }

    private VerifiedToken findVerifiedTokenByUserIdentityId(String userIdentityId) {
        log.debug("[findVerifiedTokenByUserIdentityId] Finding verified token for userId: {}", userIdentityId);

        return verifiedTokenRepository.findByUserIdentity_Id(userIdentityId)
                .orElseThrow(() -> {
                    log.error("[findVerifiedTokenByUserIdentityId] Verified token not found for userId: {}", userIdentityId);
                    return new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_NOT_EXIST);
                });
    }

    @Transactional
    @Override
    public void sendVerifiedOTP(String userIdentityId) {
        log.info("[sendVerifiedOTP] Sending verified OTP to userId: {}", userIdentityId);

        UserIdentity userIdentity = findExistingUserIdentityById(userIdentityId);
        VerifiedToken verifiedToken = findVerifiedTokenByUserIdentityId(userIdentity.getId());

        if (verifiedToken.getRetry() > VerifiedToken.MAX_RETRY) {
            log.error("[sendVerifiedOTP] Max retry exceeded for userId: {}", userIdentityId);
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.VERIFIED_TOKEN_EXCEED_MAX_RETRY);
        }

        String newOtp = otpGenerator.generateOTP();
        updateVerifiedTokenWithNewOtp(verifiedToken, newOtp);

        sendOTPEmail(userIdentity, newOtp);

        log.info("[sendVerifiedOTP] OTP sent to userId: {}", userIdentityId);
    }

    private void updateVerifiedTokenWithNewOtp(VerifiedToken verifiedToken, String otp) {
        log.debug("[updateVerifiedTokenWithNewOtp] Updating verified token with new OTP");

        verifiedToken.setRetry(verifiedToken.getRetry() + 1);
        verifiedToken.setToken(otp);
        verifiedToken.setExpiryTime(Date.from(Instant.now().plus(VERIFIED_DURATION, ChronoUnit.SECONDS)));

        verifiedTokenRepository.save(verifiedToken);

        log.debug("[updateVerifiedTokenWithNewOtp] Verified token updated with new OTP");
    }

    private void sendOTPEmail(UserIdentity userIdentity, String otp) {
        log.debug("[sendOTPEmail] Sending OTP email to user: {}", userIdentity.getEmail());

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

        kafkaProducer.send(KafkaTopics.NOTIFICATION_TOPIC, notificationEvent);

        log.debug("[sendOTPEmail] OTP email sent to: {}", userIdentity.getEmail());
    }
}
