package com.stride.tracking.profileservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeAdmin;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.profile.dto.user.request.*;
import com.stride.tracking.profile.dto.user.response.UserResponse;
import com.stride.tracking.profileservice.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/manage")
public class UserManagementController {
    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorizeAdmin
    ResponseEntity<ListResponse<UserResponse, UserFilter>> getUsers(
            @Valid AppPageRequest page,
            @Valid UserFilter filter
    ) {
        return ResponseEntity.ok(userManagementService.getUsers(page, filter));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> updateAdmin(
            @PathVariable String id,
            @RequestBody UpdateAdminRequest request
    ) {
        userManagementService.updateAdmin(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/admin")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> createAdmin(
            @RequestBody CreateAdminRequest request
    ) {
        userManagementService.createAdmin(request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PutMapping("/user/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request
    ) {
        userManagementService.updateUser(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @PostMapping("/user")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> createUser(
            @RequestBody CreateUserRequest request
    ) {
        userManagementService.createUser(request);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
