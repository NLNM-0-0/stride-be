package com.stride.tracking.identityservice.service;

import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;

public interface UserIdentityManagementService {
    void createUser(CreateUserIdentityRequest request);
    void updateAdminUserIdentity(String userId, UpdateAdminUserIdentityRequest request);
    void updateNormalUserIdentity(String userId, UpdateNormalUserIdentityRequest request);
    void resetPassword(String userId);
}
