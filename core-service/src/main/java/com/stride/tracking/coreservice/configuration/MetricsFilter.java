package com.stride.tracking.coreservice.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MetricsFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            filterChain.doFilter(request, response);
        } finally {
            String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (pattern == null) {
                pattern = request.getRequestURI();
            }

            Timer timer = Timer.builder("http.api.requests")
                    .description("HTTP server requests")
                    .tags("method", request.getMethod(),
                            "api", pattern,
                            "status", String.valueOf(response.getStatus()),
                            "service", "core-service")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry);

            sample.stop(timer);
        }
    }
}
