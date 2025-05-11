package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.persistence.BaseEntity;
import com.stride.tracking.coreservice.utils.converter.list.concrete.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "activities"
)
public class Activity extends BaseEntity {
    private static final String USER_ID_KEY = "user_id";
    private static final String SPORT_ID_KEY = "sport_id";

    @Column(name = USER_ID_KEY, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SPORT_ID_KEY, nullable = false)
    private Sport sport;

    private String routeId;

    private String name;
    private String description;

    private Long movingTimeSeconds;
    private Long elapsedTimeSeconds;
    private Integer calories;
    private Double carbonSaved;
    private Integer rpe;




    private String mapImage;

    @ElementCollection
    @Column(columnDefinition = "text[]")
    private List<String> images;





    @ElementCollection
    @Column(columnDefinition = "integer[]")
    private List<Integer> elevations;

    private Integer elevationGain;
    private Integer maxElevation;





    @ElementCollection
    @Column(columnDefinition = "double precision[]")
    private List<Double> speeds;

    private Double avgSpeed;
    private Double maxSpeed;

    @ElementCollection
    @Column(columnDefinition = "double precision[]")
    private List<Double> distances;

    private Double totalDistance;

    @Column(columnDefinition = "TEXT")
    private String geometry;

    @ElementCollection
    @Column(columnDefinition = "bigint[]")
    private List<Long> coordinatesTimestamps;





    @ElementCollection
    @Column(columnDefinition = "integer[]")
    private List<Integer> heartRates;

    @Convert(converter = HeartRateZoneValueListConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<HeartRateZoneValue> heartRateZones;

    private Double avgHearRate;
    private Integer maxHearRate;





    private Location location;
}
