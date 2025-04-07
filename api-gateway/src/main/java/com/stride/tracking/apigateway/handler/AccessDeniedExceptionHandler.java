package com.stride.tracking.apigateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stride.tracking.apigateway.exception.AuthException;
import com.stride.tracking.apigateway.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class AccessDeniedExceptionHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");

        DataBuffer buffer;
        try {
            buffer = response.bufferFactory().wrap(
                    objectMapper.writeValueAsString(
                                    ErrorResponse.builder()
                                            .status(HttpStatus.FORBIDDEN)
                                            .message(ex.getMessage())
                                            .detail(exchange.getRequest().getURI().getPath())
                                            .timestamp(new Date())
                                            .build())
                            .getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new AuthException("Can't parse to JSON error response", e);
        }

        return response.writeWith(Mono.just(buffer));
    }
}