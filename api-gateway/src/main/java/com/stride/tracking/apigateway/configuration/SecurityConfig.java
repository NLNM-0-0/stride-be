package com.stride.tracking.apigateway.configuration;

import com.stride.tracking.apigateway.handler.AccessDeniedExceptionHandler;
import com.stride.tracking.apigateway.handler.AuthenticationExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.stride.tracking.apigateway.constant.AppConstant.publicEndpoints;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtClaimsConverter jwtClaimsConverter;
    private final ReactiveJwtDecoder jwtDecoder;
    private final AccessDeniedExceptionHandler accessDeniedHandler;
    private final AuthenticationExceptionHandler authenticationExceptionHandler;

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("*"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowedMethods(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        String[] fullPublicEndpoints = addApiPrefixToEndpoints(publicEndpoints, apiPrefix);

        return http
                .cors(cors -> corsConfigurationSource())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.POST, fullPublicEndpoints).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationExceptionHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtClaimsConverter)
                                .jwtDecoder(jwtDecoder))
                )
                .build();
    }

    private String[] addApiPrefixToEndpoints(String[] endpoints, String prefix) {
        return Arrays.stream(endpoints)
                .map(endpoint -> prefix + endpoint)
                .toArray(String[]::new);
    }
}
