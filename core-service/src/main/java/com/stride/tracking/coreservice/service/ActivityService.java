package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.dto.activity.request.ActivityFilter;
import com.stride.tracking.dto.activity.request.CreateActivityRequest;
import com.stride.tracking.dto.activity.request.UpdateActivityRequest;
import com.stride.tracking.dto.activity.response.ActivityResponse;
import com.stride.tracking.dto.activity.response.ActivityShortResponse;

import java.time.ZoneId;

public interface ActivityService {
    ListResponse<ActivityShortResponse, ActivityFilter> getActivitiesOfUser(
            AppPageRequest page
    );

    ActivityResponse getActivity(String activityId);

    ActivityShortResponse createActivity(ZoneId zoneId, CreateActivityRequest activity);

    void updateActivity(String activityId, UpdateActivityRequest request);

    void deleteActivity(String activityId);
}
