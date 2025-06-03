package com.stride.tracking.metricservice.model;

import com.stride.tracking.metricservice.configuration.timescaledb.TimescaleTable;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(
        name = ActivityMetric.TABLE_NAME
)
@TimescaleTable(
        tableName = ActivityMetric.TABLE_NAME,
        timeColumnName = ActivityMetric.TIME_COLUMN_NAME
)
@IdClass(ActivityMetric.ActivityMetricId.class)
public class ActivityMetric {
    public static final String TABLE_NAME = "activity_metrics";
    public static final String TIME_COLUMN_NAME = "time";

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityMetricId implements Serializable {
        private String activityId;
        private Instant time;
    }

    @Id
    private String activityId;

    @Id
    @Column(name = TIME_COLUMN_NAME, nullable = false)
    private Instant time;

    private String userId;
    private String sportId;
    private String name;
    private String mapImage;
    private long movingTimeSeconds;
    private long elevationGain;
    private long distance;
    private Integer calories;
    private Double avgHeartRate;
}