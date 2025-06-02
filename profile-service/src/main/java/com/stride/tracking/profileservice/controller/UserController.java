package com.stride.tracking.profileservice.controller;

import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.profile.dto.profile.request.CreateProfileRequest;
import com.stride.tracking.profile.dto.profile.request.UpdateProfileRequest;
import com.stride.tracking.profile.dto.profile.response.CreateProfileResponse;
import com.stride.tracking.profile.dto.profile.response.ProfileResponse;
import com.stride.tracking.profileservice.service.impl.UserServiceImpl;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserServiceImpl userService;

    @PostMapping
    @PermitAll
    ResponseEntity<CreateProfileResponse> createUser(@RequestBody CreateProfileRequest request) {
        return new ResponseEntity<>(userService.createNewUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    @PermitAll
    ResponseEntity<ProfileResponse> viewUser() {
        return ResponseEntity.ok(userService.viewProfile());
    }

    @PutMapping("/profile")
    @PermitAll
    ResponseEntity<SimpleResponse> updateUser(
            @RequestBody UpdateProfileRequest request) {
        userService.updateUserProfile(request);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
