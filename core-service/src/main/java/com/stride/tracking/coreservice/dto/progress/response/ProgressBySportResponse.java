package com.stride.tracking.coreservice.dto.progress.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressBySportResponse {
    private Date fromDate;
    private Date toDate;
    private Long distance;
    private Long elevation;
    private Long time;
    private Long numberActivities;
}
