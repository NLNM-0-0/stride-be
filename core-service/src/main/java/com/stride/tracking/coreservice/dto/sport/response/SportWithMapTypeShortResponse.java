package com.stride.tracking.coreservice.dto.sport.response;

import com.stride.tracking.dto.sport.SportMapType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SportWithMapTypeShortResponse {
    private String id;
    private String name;
    private String image;
    private SportMapType sportMapType;
}
