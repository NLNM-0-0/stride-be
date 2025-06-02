package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.model.*;
import com.stride.tracking.coreservice.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;

public class Common {
    private Common() {
    }

    public static Category findCategoryById(String categoryId, CategoryRepository categoryRepository) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.CATEGORY_NOT_FOUND)
        );
    }

    public static Sport findSportById(String sportId, SportRepository sportRepository) {
        return sportRepository.findById(sportId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.SPORT_NOT_FOUND)
        );
    }

    public static Sport findReadOnlySportById(
            String sportId,
            SportRepository sportRepository,
            EntityManager entityManager
    ) {
        Sport sport = findSportById(sportId, sportRepository);
        entityManager.detach(sport);
        return sport;
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

    public static Route findRouteById(String routeId, RouteRepository routeRepository) {
        return routeRepository.findById(routeId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.ROUTE_NOT_FOUND)
        );
    }
}
