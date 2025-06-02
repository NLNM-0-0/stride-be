package com.stride.tracking.metricservice.client;

import com.stride.tracking.commons.configuration.feign.FeignConfig;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.metric.dto.category.response.CategoryResponse;
import com.stride.tracking.metric.dto.sport.response.SportShortResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "core-service",
        url = "${app.services.core}",
        configuration = FeignConfig.class
)
@Component
public interface CoreFeignClient {
    @GetMapping(value = "/sports/all", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SimpleListResponse<SportShortResponse>> getAllSports();

    @GetMapping(value = "/categories/all", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SimpleListResponse<CategoryResponse>> getAllCategories();
}
