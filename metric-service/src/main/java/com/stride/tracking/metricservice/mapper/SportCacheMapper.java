package com.stride.tracking.metricservice.mapper;

import com.stride.tracking.core.dto.sport.event.SportUpdatedEvent;
import com.stride.tracking.metric.dto.sport.SportMapType;
import com.stride.tracking.metric.dto.sport.response.SportShortResponse;
import com.stride.tracking.metricservice.model.SportCache;
import org.springframework.stereotype.Component;

@Component
public class SportCacheMapper {
    public SportCache mapToModel(SportUpdatedEvent event) {
        return SportCache.builder()
                .id(event.getId())
                .name(event.getName())
                .image(event.getImage())
                .color(event.getColor())
                .sportMapType(SportMapType.valueOf(event.getSportMapType().toString()))
                .build();
    }

    public SportCache mapToModel(SportShortResponse sport) {
        return SportCache.builder()
                .id(sport.getId())
                .name(sport.getName())
                .image(sport.getImage())
                .color(sport.getColor())
                .sportMapType(sport.getSportMapType())
                .build();
    }

    public SportShortResponse mapToShortResponse(SportCache sport) {
        return SportShortResponse.builder()
                .id(sport.getId())
                .name(sport.getName())
                .image(sport.getImage())
                .color(sport.getColor())
                .sportMapType(sport.getSportMapType())
                .build();
    }
}
