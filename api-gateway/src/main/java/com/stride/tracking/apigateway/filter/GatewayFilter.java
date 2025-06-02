package com.stride.tracking.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stride.tracking.apigateway.constant.CustomHeaders;
import com.stride.tracking.apigateway.constant.Message;
import com.stride.tracking.apigateway.exception.AuthException;
import com.stride.tracking.apigateway.exception.ErrorResponse;
import com.stride.tracking.apigateway.service.IdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.stride.tracking.apigateway.constant.AppConstant.publicEndpoints;

@Component
@RequiredArgsConstructor
public class GatewayFilter implements GlobalFilter {
    private final IdentityService identityService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String timezone = Optional.ofNullable(
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CustomHeaders.X_USER_TIMEZONE)
        ).orElse("UTC");

        if (isPublicEndpoint(exchange.getRequest().getPath().value())) {
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(httpHeaders -> httpHeaders.set(CustomHeaders.X_USER_TIMEZONE, timezone))
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);
        }

        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader)) {
            return unauthenticated(exchange.getRequest(), exchange.getResponse());
        }

        String token = authHeader.get(0).replace("Bearer ", "");

        return identityService.introspect(token).flatMap(response -> {
            if (response.getStatusCode() != HttpStatus.OK ||
                    response.getBody() == null ||
                    !response.getBody().isValid()) {
                return unauthenticated(exchange.getRequest(), exchange.getResponse());
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(httpHeaders -> {
                        httpHeaders.set(CustomHeaders.X_AUTH_USER_ID, response.getBody().getUserId());
                        httpHeaders.set(CustomHeaders.X_AUTH_USERNAME, response.getBody().getUsername());
                        httpHeaders.set(CustomHeaders.X_AUTH_EMAIL, response.getBody().getEmail());
                        httpHeaders.set(CustomHeaders.X_AUTH_PROVIDER, response.getBody().getProvider());
                        httpHeaders.set(CustomHeaders.X_AUTH_USER_AUTHORITIES, response.getBody().getScope());
                        httpHeaders.set(CustomHeaders.X_USER_TIMEZONE, timezone);
                    })
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);

        }).onErrorResume(throwable -> unauthenticated(exchange.getRequest(), exchange.getResponse()));
    }

    boolean isPublicEndpoint(String endpoint) {
        PathPatternParser parser = new PathPatternParser();
        return Arrays.stream(publicEndpoints)
                .map(parser::parse)
                .anyMatch(pattern -> pattern.matches(PathContainer.parsePath(endpoint)));
    }

    Mono<Void> unauthenticated(ServerHttpRequest request, ServerHttpResponse response) {
        ErrorResponse errorResponse = new ErrorResponse(
                new Date(),
                HttpStatus.UNAUTHORIZED,
                Message.UNAUTHORIZED,
                request.getPath().toString()
        );

        String body;
        try {
            body = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            throw new AuthException("Can't parse to JSON error response", e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }
}
