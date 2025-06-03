package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.activity.request.ActivityFilter;
import com.stride.tracking.core.dto.activity.request.CreateActivityRequest;
import com.stride.tracking.core.dto.activity.request.UpdateActivityRequest;
import com.stride.tracking.core.dto.activity.response.ActivityResponse;
import com.stride.tracking.core.dto.activity.response.ActivityShortResponse;

import java.time.ZoneId;

public interface ActivityService {
    ListResponse<ActivityShortResponse, ActivityFilter> getActivitiesOfUser(
            ZoneId zoneId,
            AppPageRequest page,
            ActivityFilter filter
    );

    ActivityResponse getActivity(String activityId);

    ActivityShortResponse createActivity(ZoneId zoneId, CreateActivityRequest activity);

    void updateActivity(String activityId, UpdateActivityRequest request);

    void deleteActivity(String activityId);
}
