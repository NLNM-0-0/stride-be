package com.stride.tracking.coreservice.dto.progress.response;

import com.stride.tracking.coreservice.dto.sport.response.SportShortResponse;
import com.stride.tracking.coreservice.dto.sport.response.SportWithMapTypeShortResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressResponse {
    private SportWithMapTypeShortResponse sport;

    private List<ProgressBySportResponse> progresses;
}
