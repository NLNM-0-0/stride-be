package com.stride.tracking.profileservice.service;


import com.stride.tracking.dto.request.CreateUserRequest;
import com.stride.tracking.dto.request.UpdateUserRequest;
import com.stride.tracking.dto.response.CreateUserResponse;
import com.stride.tracking.dto.response.UserResponse;

public interface UserService {
    CreateUserResponse createNewUser(CreateUserRequest request);
    UserResponse viewProfile();
    void updateUserProfile(UpdateUserRequest request);
}
