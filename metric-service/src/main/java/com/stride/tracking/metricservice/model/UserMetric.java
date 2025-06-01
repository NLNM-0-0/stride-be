package com.stride.tracking.metricservice.model;

import com.stride.tracking.metric.dto.report.AuthProvider;
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
        name = UserMetric.TABLE_NAME
)
@TimescaleTable(
        tableName = UserMetric.TABLE_NAME,
        timeColumnName = UserMetric.TIME_COLUMN_NAME
)
@IdClass(UserMetric.UserMetricId.class)
public class UserMetric {
    public static final String TABLE_NAME = "user_metrics";
    public static final String TIME_COLUMN_NAME = "time";

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMetricId implements Serializable {
        private String userId;
        private Instant time;
    }

    @Id
    private String userId;

    @Id
    @Column(name = TIME_COLUMN_NAME, nullable = false)
    private Instant time;

    private AuthProvider provider;
}