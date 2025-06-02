package com.stride.tracking.profileservice.service;

import com.stride.tracking.profile.dto.profile.request.CreateProfileRequest;
import com.stride.tracking.profile.dto.profile.request.UpdateProfileRequest;
import com.stride.tracking.profile.dto.profile.response.CreateProfileResponse;
import com.stride.tracking.profile.dto.profile.response.ProfileResponse;

public interface UserService {
    CreateProfileResponse createNewUser(CreateProfileRequest request);
    ProfileResponse viewProfile();
    void updateUserProfile(UpdateProfileRequest request);
}
