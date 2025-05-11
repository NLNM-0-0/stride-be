package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.constant.GoalTimeFrame;
import com.stride.tracking.coreservice.constant.GoalType;
import com.stride.tracking.coreservice.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "goals"
)
public class Goal extends BaseEntity {
    private static final String USER_ID_KEY = "user_id";
    private static final String SPORT_ID_KEY = "sport_id";

    @Column(name = USER_ID_KEY, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SPORT_ID_KEY, nullable = false)
    private Sport sport;

    private GoalType type;
    private GoalTimeFrame timeFrame;

    private Long amount;
    private boolean active;
}
