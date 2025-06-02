package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.persistence.BaseEntity;
import com.stride.tracking.coreservice.utils.converter.list.concrete.StringListConverter;
import com.stride.tracking.coreservice.utils.converter.map.concrete.MapStringListString;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.ColumnTransformer;
import org.locationtech.jts.geom.Geometry;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "routes"
)
public class Route  extends BaseEntity {
    private static final String USER_ID_KEY = "user_id";
    private static final String SPORT_ID_KEY = "sport_id";

    @Column(name = USER_ID_KEY)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SPORT_ID_KEY, nullable = false)
    private Sport sport;

    @Column(name = "name", nullable = false)
    private String name;

    @ColumnDefault("0")
    private Double totalTime = 0d;

    @ColumnDefault("0")
    private Double totalDistance = 0d;

    private Location location;

    private String mapImage;

    @Convert(converter = MapStringListString.class)
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private Map<String, List<String>> images;

    @Column(columnDefinition = "geometry(LineString,4326)")
    private Geometry geometry;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private List<String> districts;

    @ColumnDefault("0")
    private Integer heat = 0;
}
