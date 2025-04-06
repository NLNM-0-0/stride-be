package com.stride.tracking.identityservice.controller;

import com.stride.tracking.commons.response.SimpleResponse;
import com.stride.tracking.dto.request.AuthenticationRequest;
import com.stride.tracking.dto.request.IntrospectRequest;
import com.stride.tracking.dto.request.LogoutRequest;
import com.stride.tracking.dto.response.AuthenticationResponse;
import com.stride.tracking.dto.response.IntrospectResponse;
import com.stride.tracking.identityservice.service.impl.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse result = authenticationService.authenticate(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/introspect")
    ResponseEntity<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        IntrospectResponse result = authenticationService.introspect(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    ResponseEntity<SimpleResponse> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
