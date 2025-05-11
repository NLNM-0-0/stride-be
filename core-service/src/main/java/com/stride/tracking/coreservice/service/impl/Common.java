package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.model.*;
import com.stride.tracking.coreservice.repository.*;
import org.springframework.http.HttpStatus;

public class Common {
    private Common() {
    }

    public static Activity findActivityById(String activityId, ActivityRepository activityRepository) {
        return activityRepository.findById(activityId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.ACTIVITY_NOT_FOUND)
        );
    }

    public static Goal findGoalById(String goalId, GoalRepository goalRepository) {
        return goalRepository.findById(goalId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.GOAL_NOT_FOUND)
        );
    }
}
