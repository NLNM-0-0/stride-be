package com.stride.tracking.identityservice.service;

import com.stride.tracking.identity.dto.auth.request.AuthenticateWithGoogleRequest;
import com.stride.tracking.identity.dto.auth.request.AuthenticationRequest;
import com.stride.tracking.identity.dto.auth.request.IntrospectRequest;
import com.stride.tracking.identity.dto.auth.request.LogoutRequest;
import com.stride.tracking.identity.dto.auth.response.AuthenticationResponse;
import com.stride.tracking.identity.dto.auth.response.IntrospectResponse;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void logout(LogoutRequest request);

    AuthenticationResponse authenticateWithGoogle(AuthenticateWithGoogleRequest idToken);
}
