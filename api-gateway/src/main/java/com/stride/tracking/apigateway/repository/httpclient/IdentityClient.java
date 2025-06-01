package com.stride.tracking.apigateway.repository.httpclient;

import com.stride.tracking.identity.dto.auth.request.IntrospectRequest;
import com.stride.tracking.identity.dto.auth.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@Component
public interface IdentityClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
