package com.stride.tracking.coreservice.dto.progress.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProgressActivityRequest {
    private Date fromDate;
    private Date toDate;
}
