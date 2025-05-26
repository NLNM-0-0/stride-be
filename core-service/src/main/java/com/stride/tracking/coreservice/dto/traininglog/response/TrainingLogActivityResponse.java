package com.stride.tracking.coreservice.dto.traininglog.response;

import com.stride.tracking.coreservice.dto.sport.response.SportShortResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingLogActivityResponse {
    private String id;
    private String name;
    private Date date;
    private SportShortResponse sport;
    private long distance;
    private long elevation;
    private long time;
}
