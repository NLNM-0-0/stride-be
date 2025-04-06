package com.stride.tracking.identityservice.service.impl;

import com.stride.tracking.dto.request.AuthenticationRequest;
import com.stride.tracking.dto.request.IntrospectRequest;
import com.stride.tracking.dto.request.LogoutRequest;
import com.stride.tracking.dto.response.AuthenticationResponse;
import com.stride.tracking.dto.response.IntrospectResponse;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void logout(LogoutRequest request);
}
