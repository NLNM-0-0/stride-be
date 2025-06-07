package com.stride.tracking.metricservice.controller;

import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.core.dto.category.event.CategoryUpdatedEvent;
import com.stride.tracking.metricservice.service.impl.CategoryCacheService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryCacheUpdateKafkaController {
    private final CategoryCacheService categoryCacheService;

    @KafkaListener(topics = KafkaTopics.CATEGORY_UPDATED_TOPIC)
    @PermitAll
    public void listenCategoryUpdatedEvent(CategoryUpdatedEvent event) {
        categoryCacheService.updateCategory(event);
    }

    @KafkaListener(topics = KafkaTopics.CATEGORY_CREATED_TOPIC)
    @PermitAll
    public void listenCategoryCreatedEvent(CategoryUpdatedEvent event) {
        categoryCacheService.updateCategory(event);
    }
}
