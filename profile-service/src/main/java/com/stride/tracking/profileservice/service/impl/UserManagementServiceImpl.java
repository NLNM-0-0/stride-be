package com.stride.tracking.profileservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;
import com.stride.tracking.metric.dto.report.AuthProvider;
import com.stride.tracking.metric.dto.user.event.UserCreatedEvent;
import com.stride.tracking.profile.dto.user.request.*;
import com.stride.tracking.profile.dto.user.response.UserResponse;
import com.stride.tracking.profileservice.constant.Message;
import com.stride.tracking.profileservice.mapper.UserMapper;
import com.stride.tracking.profileservice.model.User;
import com.stride.tracking.profileservice.repository.UserRepository;
import com.stride.tracking.profileservice.repository.spec.UserCriteria;
import com.stride.tracking.profileservice.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final UserRepository userRepository;

    private final MongoTemplate mongoTemplate;

    private final IdentityService identityService;

    private final UserMapper userMapper;

    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional(readOnly = true)
    public ListResponse<UserResponse, UserFilter> getUsers(
            AppPageRequest page,
            UserFilter filter
    ) {
        String userId = SecurityUtils.getCurrentUserId();

        Criteria criteria = filterUsers(filter, userId);

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.ASC, "name"))
                .with(PageRequest.of(page.getPage() - 1, page.getLimit()));

        long totalElements = mongoTemplate.count(new Query(criteria), User.class);
        int totalPages = (int) Math.ceil((double) totalElements / page.getLimit());

        List<User> users = mongoTemplate.find(query, User.class);

        List<UserResponse> data = users.stream()
                .map(userMapper::mapToUserManagementResponse)
                .toList();

        return ListResponse.<UserResponse, UserFilter>builder()
                .data(data)
                .filter(filter)
                .appPageResponse(
                        AppPageResponse.builder()
                                .index(page.getPage())
                                .limit(page.getLimit())
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .build()
                )
                .build();
    }

    Criteria filterUsers(UserFilter filter, String currentUserId) {
        UserCriteria criteria = new UserCriteria();
        criteria = criteria.doNotHaveId(currentUserId);
        if (filter.getIsAdmin() != null) {
            criteria = criteria.isAdmin(filter.getIsAdmin());
        }
        if (filter.getIsBlocked() != null) {
            criteria = criteria.isBlocked(filter.getIsBlocked());
        }
        if (filter.getSearch() != null) {
            criteria = criteria.hasNameOrEmailOrDobContains(filter.getSearch());
        }
        return criteria.build();
    }

    @Override
    @Transactional
    public void updateUser(String userId, UpdateUserRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_UPDATE_YOURSELF);
        }

        User user = Common.findUserById(userId, userRepository);
        UpdateHelper.updateIfNotNull(request.getIsBlocked(), user::setBlocked);

        if (request.getIsBlocked() != null) {
            identityService.updateUserIdentity(
                    userId,
                    UpdateNormalUserIdentityRequest.builder()
                            .isBlocked(request.getIsBlocked())
                            .build()
            );
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateAdmin(String userId, UpdateAdminRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOT_UPDATE_YOURSELF);
        }

        User user = Common.findUserById(userId, userRepository);
        UpdateHelper.updateIfNotNull(request.getIsBlocked(), user::setBlocked);
        UpdateHelper.updateIfNotNull(request.getAva(), user::setAva);
        UpdateHelper.updateIfNotNull(request.getDob(), user::setDob);
        UpdateHelper.updateIfNotNull(request.getName(), user::setName);
        UpdateHelper.updateIfNotNull(request.getEmail(), user::setEmail);

        if (request.getIsBlocked() != null || request.getEmail() != null) {
            identityService.updateUserIdentity(
                    userId,
                    UpdateAdminUserIdentityRequest.builder()
                            .email(request.getEmail())
                            .isBlocked(request.getIsBlocked())
                            .build()
            );
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void createUser(CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getEmail())
                .isBlocked(false)
                .isAdmin(false)
                .build();

        user = userRepository.save(user);

        identityService.createUserIdentity(CreateUserIdentityRequest.builder()
                .userId(user.getId())
                .email(request.getEmail())
                .password(request.getPassword())
                .isAdmin(false)
                .build());

        sendUserCreatedMetric(user);
    }

    @Override
    @Transactional
    public void createAdmin(CreateAdminRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getEmail())
                .ava(request.getAva())
                .dob(request.getDob())
                .isBlocked(false)
                .isAdmin(true)
                .build();

        user = userRepository.save(user);

        identityService.createUserIdentity(CreateUserIdentityRequest.builder()
                .userId(user.getId())
                .email(request.getEmail())
                .password(request.getPassword())
                .isAdmin(true)
                .build());

        sendUserCreatedMetric(user);
    }

    private void sendUserCreatedMetric(User user) {
        kafkaProducer.send(
                KafkaTopics.USER_CREATED_TOPIC,
                UserCreatedEvent.builder()
                        .time(user.getCreatedAt())
                        .provider(AuthProvider.STRIDE.toString())
                        .userId(user.getId())
                        .build()
        );
    }
}
