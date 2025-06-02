package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;
import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.constant.Message;
import com.stride.tracking.identityservice.model.UserIdentity;
import com.stride.tracking.identityservice.repository.UserIdentityRepository;
import com.stride.tracking.identityservice.service.UserIdentityManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserIdentityManagementServiceImpl implements UserIdentityManagementService {
    private final UserIdentityRepository userIdentityRepository;

    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "App123";

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
                .isVerified(request.getIsAdmin())
                .isAdmin(request.getIsAdmin())
                .build();

        userIdentityRepository.save(userIdentity);
    }

    private void validateEmailNotTaken(String email) {
        boolean emailTaken = userIdentityRepository.findByProviderAndEmail(AuthProvider.STRIDE, email).isPresent();
        if (emailTaken) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.USER_EXISTED);
        }
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

    private UserIdentity findStrideUserIdentityByProviderAndUserId(String userId){
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
            if(userIdentity.isAdmin()) {
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

        if (!userIdentity.getUserId().equals(currentUserId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_RESET_YOUR_PASSWORD);
        }
    }
}
