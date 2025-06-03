package com.stride.tracking.metricservice.mapper;

import com.stride.tracking.core.dto.sport.event.SportUpdatedEvent;
import com.stride.tracking.metric.dto.category.response.CategoryResponse;
import com.stride.tracking.metric.dto.sport.SportMapType;
import com.stride.tracking.metric.dto.sport.response.SportShortResponse;
import com.stride.tracking.metric.dto.sport.response.SportWithoutCategoryResponse;
import com.stride.tracking.metricservice.model.SportCache;
import com.stride.tracking.metricservice.service.impl.CategoryCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SportCacheMapper {
    private final CategoryCacheService categoryCacheService;

    public SportCache mapToModel(SportUpdatedEvent event) {
        SportMapType sportMapType = event.getSportMapType() == null ?
                null :
                SportMapType.valueOf(event.getSportMapType().toString());

        return SportCache.builder()
                .id(event.getId())
                .name(event.getName())
                .image(event.getImage())
                .color(event.getColor())
                .categoryId(event.getCategoryId())
                .sportMapType(sportMapType)
                .build();
    }

    public SportCache mapToModel(SportShortResponse sport) {
        return SportCache.builder()
                .id(sport.getId())
                .name(sport.getName())
                .image(sport.getImage())
                .color(sport.getColor())
                .categoryId(sport.getCategory().getId())
                .sportMapType(sport.getSportMapType())
                .build();
    }

    public SportShortResponse mapToShortResponse(SportCache sport) {
        String categoryName = categoryCacheService.getCategory(sport.getCategoryId());

        return SportShortResponse.builder()
                .id(sport.getId())
                .name(sport.getName())
                .image(sport.getImage())
                .color(sport.getColor())
                .category(
                        CategoryResponse.builder()
                                .id(sport.getCategoryId())
                                .name(categoryName).build()
                )
                .sportMapType(sport.getSportMapType())
                .build();
    }

    public SportWithoutCategoryResponse mapToDetailReportResponse(SportCache sport) {
        return SportWithoutCategoryResponse.builder()
                .id(sport.getId())
                .name(sport.getName())
                .image(sport.getImage())
                .color(sport.getColor())
                .sportMapType(sport.getSportMapType())
                .build();
    }
}
