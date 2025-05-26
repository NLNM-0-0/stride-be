package com.stride.tracking.coreservice.dto.traininglog.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingLogResponse {
    private Date date;
    private String color;
    private List<TrainingLogActivityResponse> activities;
    private long distance;
    private long elevation;
    private long time;
}
