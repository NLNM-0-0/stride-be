package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.persistence.BaseEntity;
import com.stride.tracking.coreservice.utils.converter.list.concrete.*;
import com.stride.tracking.coreservice.utils.converter.map.concrete.HeartRateZoneMapConverter;
import com.stride.tracking.dto.user.HeartRateZone;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "activities"
)
public class Activity extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
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

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<String> images;





    @Convert(converter = IntegerListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<Integer> elevations;

    private Integer elevationGain;
    private Integer maxElevation;





    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<Double> speeds;

    private Double avgSpeed;
    private Double maxSpeed;

    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<Double> distances;

    private Double totalDistance;

    @Column(columnDefinition = "TEXT")
    private String geometry;

    @Convert(converter = LongListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<Long> coordinatesTimestamps;





    @Convert(converter = IntegerListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<Integer> heartRates;

    @Convert(converter = HeartRateZoneValueListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<HeartRateZoneValue> heartRateZones;

    private Double avgHearRate;
    private Double maxHearRate;
}
