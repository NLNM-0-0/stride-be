package com.stride.tracking.profileservice.service;

import com.stride.tracking.profile.dto.user.request.CreateUserRequest;
import com.stride.tracking.profile.dto.user.request.UpdateUserRequest;
import com.stride.tracking.profile.dto.user.response.CreateUserResponse;
import com.stride.tracking.profile.dto.user.response.UserResponse;

public interface UserService {
    CreateUserResponse createNewUser(CreateUserRequest request);
    UserResponse viewProfile();
    void updateUserProfile(UpdateUserRequest request);
}
