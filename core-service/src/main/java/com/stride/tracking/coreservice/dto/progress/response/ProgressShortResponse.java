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
public class ProgressShortResponse {
    private Long amount;
    private Long numberActivities;
    private Date fromDate;
    private Date toDate;
}
