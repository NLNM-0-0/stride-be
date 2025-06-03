package com.stride.tracking.identityservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeAdmin;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.identity.dto.user.request.CreateUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateAdminUserIdentityRequest;
import com.stride.tracking.identity.dto.user.request.UpdateNormalUserIdentityRequest;
import com.stride.tracking.identityservice.service.UserIdentityManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage")
@RequiredArgsConstructor
@Slf4j
public class UserIdentityManagementController {
    private final UserIdentityManagementService identityManagementService;

    @PutMapping("/admin/by-user-id/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> updateAdminUserIdentity(
            @PathVariable String id,
            @Valid @RequestBody UpdateAdminUserIdentityRequest request
    ) {
        identityManagementService.updateAdminUserIdentity(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PutMapping("/normal/by-user-id/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> updateNormalUserIdentity(
            @PathVariable String id,
            @Valid @RequestBody UpdateNormalUserIdentityRequest request
    ) {
        identityManagementService.updateNormalUserIdentity(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> createUserIdentity(
            @Valid @RequestBody CreateUserIdentityRequest request
    ) {
        identityManagementService.createUser(request);
        return new ResponseEntity<>(new SimpleResponse(), HttpStatus.CREATED);
    }

    @PostMapping("/admin/by-user-id/{id}/reset-password")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> resetPassword(
            @PathVariable String id
    ) {
        identityManagementService.resetPassword(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
