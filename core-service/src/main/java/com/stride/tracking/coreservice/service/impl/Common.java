package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.model.Activity;
import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.repository.ActivityRepository;
import com.stride.tracking.coreservice.repository.CategoryRepository;
import com.stride.tracking.coreservice.repository.SportRepository;
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

    public static Activity findActivityById(String activityId, ActivityRepository activityRepository) {
        return activityRepository.findById(activityId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.SPORT_NOT_FOUND)
        );
    }
}
