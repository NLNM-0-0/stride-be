package com.stride.tracking.coreservice.dto.progress.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProgressActivityRequest {
    private Long fromDate;
    private Long toDate;
    private String sportId;
}
