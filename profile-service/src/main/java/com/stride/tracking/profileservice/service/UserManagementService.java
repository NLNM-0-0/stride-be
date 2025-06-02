package com.stride.tracking.profileservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.profile.dto.user.request.*;
import com.stride.tracking.profile.dto.user.response.UserResponse;

public interface UserManagementService {
    ListResponse<UserResponse, UserFilter> getUsers(
            AppPageRequest page,
            UserFilter filter
    );

    void updateUser(String userId, UpdateUserRequest request);

    void updateAdmin(String userId, UpdateAdminRequest request);

    void createUser(CreateUserRequest request);

    void createAdmin(CreateAdminRequest request);
}
