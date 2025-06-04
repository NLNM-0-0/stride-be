package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.bridge.dto.email.event.SendEmailEvent;
import com.stride.tracking.bridge.dto.email.request.Recipient;
import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;
import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.model.UserIdentity;
import com.stride.tracking.identityservice.model.VerifiedToken;
import com.stride.tracking.identityservice.repository.UserIdentityRepository;
import com.stride.tracking.identityservice.repository.VerifiedTokenRepository;
import com.stride.tracking.identityservice.service.UserIdentityManagementService;
import com.stride.tracking.identityservice.utils.OTPGenerator;
import com.stride.tracking.identityservice.utils.mail.MailFormatGenerator;
import com.stride.tracking.identityservice.utils.mail.MailFormatGeneratorFactory;
import com.stride.tracking.identityservice.utils.mail.MailType;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserIdentityManagementServiceImpl implements UserIdentityManagementService {
    private final UserIdentityRepository userIdentityRepository;
    private final VerifiedTokenRepository verifiedTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final OTPGenerator otpGenerator;

    private final KafkaProducer kafkaProducer;

    private static final String DEFAULT_PASSWORD = "App123";

    @NonFinal
    @Value("${verified-token.duration}")
    protected long VERIFIED_DURATION;

    @Override
    public void createUser(CreateUserIdentityRequest request) {
        validateEmailNotTaken(request.getEmail());

        UserIdentity userIdentity = UserIdentity.builder()
                .userId(request.getUserId())
                .provider(AuthProvider.STRIDE)
                .username(request.getEmail())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isBlocked(false)
                .isVerified(request.isAdmin())
                .isAdmin(request.isAdmin())
                .build();
        userIdentityRepository.save(userIdentity);

        if (!request.isAdmin()) {
            String otp = saveVerifiedToken(userIdentity);
            sendVerifiedEmail(userIdentity, otp);
        }
    }

    private void validateEmailNotTaken(String email) {
        boolean emailTaken = userIdentityRepository.findByProviderAndEmail(AuthProvider.STRIDE, email).isPresent();
        if (emailTaken) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_EXISTED);
        }
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
        MailFormatGenerator generator = MailFormatGeneratorFactory.getGenerator(MailType.VERIFY_ACCOUNT);

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
    }


    @Override
    public void updateAdminUserIdentity(String userId, UpdateAdminUserIdentityRequest request) {
        UserIdentity userIdentity = findStrideUserIdentityByProviderAndUserId(userId);

        validateAdmin(userIdentity);

        if (request.getEmail() != null && !request.getEmail().equals(userIdentity.getEmail())) {
            validateEmailNotTaken(request.getEmail());
            userIdentity.setEmail(request.getEmail());
            userIdentity.setUsername(request.getEmail());
        }

        UpdateHelper.updateIfNotNull(request.getIsBlocked(), userIdentity::setBlocked);

        userIdentityRepository.save(userIdentity);
    }

    private UserIdentity findStrideUserIdentityByProviderAndUserId(String userId) {
        return userIdentityRepository.findByProviderAndUserId(
                AuthProvider.STRIDE,
                userId
        ).orElseThrow(() -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST));
    }

    private void validateAdmin(UserIdentity userIdentity) {
        if (!userIdentity.isAdmin()) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_IS_NOT_ADMIN);
        }
    }

    @Override
    public void updateNormalUserIdentity(String userId, UpdateNormalUserIdentityRequest request) {
        List<UserIdentity> userIdentities = userIdentityRepository.findAllByUserId(
                userId
        );

        for (UserIdentity userIdentity : userIdentities) {
            if (userIdentity.isAdmin()) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_IS_ADMIN);
            }

            UpdateHelper.updateIfNotNull(request.getIsBlocked(), userIdentity::setBlocked);
        }

        userIdentityRepository.saveAll(userIdentities);
    }

    @Override
    @Transactional
    public void resetPassword(String userId) {
        UserIdentity userIdentity = findStrideUserIdentityByProviderAndUserId(userId);

        validateAdmin(userIdentity);
        validateNotCurrentUser(userIdentity);

        userIdentity.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

        userIdentityRepository.save(userIdentity);
    }

    private void validateNotCurrentUser(UserIdentity userIdentity) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        if (userIdentity.getUserId().equals(currentUserId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_RESET_YOUR_PASSWORD);
        }
    }
}
