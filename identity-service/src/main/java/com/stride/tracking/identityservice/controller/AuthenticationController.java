package com.stride.tracking.identityservice.controller;

import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.dto.auth.request.AuthenticateWithGoogleRequest;
import com.stride.tracking.dto.auth.request.AuthenticationRequest;
import com.stride.tracking.dto.auth.request.IntrospectRequest;
import com.stride.tracking.dto.auth.request.LogoutRequest;
import com.stride.tracking.dto.auth.response.AuthenticationResponse;
import com.stride.tracking.dto.auth.response.IntrospectResponse;
import com.stride.tracking.identityservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/login/google")
    ResponseEntity<AuthenticationResponse> authenticateWithGoogle(@RequestBody AuthenticateWithGoogleRequest request) {
        AuthenticationResponse result = authenticationService.authenticateWithGoogle(request);
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
