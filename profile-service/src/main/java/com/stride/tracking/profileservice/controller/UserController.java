package com.stride.tracking.profileservice.controller;

import com.stride.tracking.commons.response.SimpleResponse;
import com.stride.tracking.dto.request.CreateUserRequest;
import com.stride.tracking.dto.request.UpdateUserRequest;
import com.stride.tracking.dto.response.CreateUserResponse;
import com.stride.tracking.dto.response.UserResponse;
import com.stride.tracking.profileservice.service.impl.UserServiceImpl;
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
    ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request) {
        return new ResponseEntity<>(userService.createNewUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    ResponseEntity<UserResponse> viewUser() {
        return ResponseEntity.ok(userService.viewProfile());
    }

    @PutMapping("/profile")
    ResponseEntity<SimpleResponse> updateUser(
            @RequestBody UpdateUserRequest request) {
        userService.updateUserProfile(request);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
