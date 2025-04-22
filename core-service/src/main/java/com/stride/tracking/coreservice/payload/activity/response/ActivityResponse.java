package com.stride.tracking.coreservice.payload.activity.response;

import com.stride.tracking.coreservice.payload.sport.response.SportResponse;
import com.stride.tracking.dto.constant.HeartRateZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private String id;
    private String name;
    private String description;
    private SportResponse sport;
    private ActivityUserResponse user;
    private Double totalDistance;
    private Long elapsedTimeSeconds;
    private Long movingTimeSeconds;
    private Integer calories;
    private Double carbonSaved;
    private Integer rpe;
    private List< List<Double>> coordinates;

    private List<String> images;
    private String mapImage;

    private List<Integer> elevations;
    private Integer elevationGain;
    private Integer maxElevation;

    private List<Double> speeds;
    private Double avgSpeed;
    private Double maxSpeed;

    private List<Integer> heartRates;
    private Map<HeartRateZone, Integer> heartRateZones;
    private Double avgHearRate;
    private Double maxHearRate;

    private Date createdAt;
}
