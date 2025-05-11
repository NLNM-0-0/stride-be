package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.persistence.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "goal_histories"
)
public class GoalHistory extends BaseEntity {
    private static final String GOAL_ID_KEY = "goal_id";
    private static final String ACTIVITY_ID_KEY = "activity_id";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = GOAL_ID_KEY, nullable = false)
    private Goal goal;

    private String dateKey;

    private Long amountGain;
    private Long amountGoal;

    private Date date;
}
