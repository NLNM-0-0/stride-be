package com.stride.tracking.coreservice.dto.progress.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProgressActivityResponse {
    private Long distance;
    private Long elevation;
    private Long time;
    private List<ProgressActivityResponse> activities;
}
