package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "progress"
)
public class Progress extends BaseEntity {
    private static final String USER_ID_KEY = "user_id";
    private static final String SPORT_ID_KEY = "sport_id";
    private static final String ACTIVITY_ID_KEY = "activity_id";

    @Column(name = USER_ID_KEY, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SPORT_ID_KEY, nullable = false)
    private Sport sport;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ACTIVITY_ID_KEY, nullable = false, unique = true)
    private Activity activity;

    private long time;
    private long distance;
    private long elevation;
}
