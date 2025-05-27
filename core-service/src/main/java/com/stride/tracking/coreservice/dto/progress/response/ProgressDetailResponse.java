package com.stride.tracking.coreservice.dto.progress.response;

import com.stride.tracking.coreservice.constant.ProgressTimeFrame;
import com.stride.tracking.coreservice.dto.sport.response.SportShortResponse;
import com.stride.tracking.coreservice.dto.sport.response.SportWithMapTypeShortResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressDetailResponse {
    private SportWithMapTypeShortResponse sport;
    private List<SportWithMapTypeShortResponse> availableSports;

    private Map<ProgressTimeFrame, List<ProgressBySportResponse>> progresses;
}
