package com.stride.tracking.apigateway.service;

import com.stride.tracking.apigateway.repository.httpclient.IdentityClient;
import com.stride.tracking.dto.auth.request.IntrospectRequest;
import com.stride.tracking.dto.auth.response.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IdentityService {
    private final IdentityClient identityClient;

    public Mono<ResponseEntity<IntrospectResponse>> introspect(String token) {
        return identityClient.introspect(IntrospectRequest.builder()
                .token(token)
                .build());
    }
}
