package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeUser;
import com.stride.tracking.commons.constants.CustomHeaders;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.activity.request.ActivityFilter;
import com.stride.tracking.core.dto.activity.request.CreateActivityRequest;
import com.stride.tracking.core.dto.activity.request.UpdateActivityRequest;
import com.stride.tracking.core.dto.activity.response.ActivityResponse;
import com.stride.tracking.core.dto.activity.response.ActivityShortResponse;
import com.stride.tracking.coreservice.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping("/users/profile")
    @PreAuthorizeUser
    ResponseEntity<ListResponse<ActivityShortResponse, ActivityFilter>> getActivitiesOfUser(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone,
            @Valid AppPageRequest page) {
        ZoneId zoneId = ZoneId.of(timezone);

        return ResponseEntity.ok(activityService.getActivitiesOfUser(
                zoneId,
                page,
                new ActivityFilter()
        ));
    }

    @GetMapping("/users/profile/filter")
    @PreAuthorizeUser
    ResponseEntity<ListResponse<ActivityShortResponse, ActivityFilter>> getActivitiesOfUser(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone,
            @Valid AppPageRequest page,
            @Valid ActivityFilter filter
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        return ResponseEntity.ok(activityService.getActivitiesOfUser(zoneId, page, filter));
    }

    @GetMapping("/{id}")
    @PreAuthorizeUser
    ResponseEntity<ActivityResponse> getActivity(
            @PathVariable String id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @PostMapping
    @PreAuthorizeUser
    ResponseEntity<ActivityShortResponse> createActivity(
            @RequestBody CreateActivityRequest request,
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        ActivityShortResponse response = activityService.createActivity(zoneId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorizeUser
    ResponseEntity<SimpleResponse> updateActivity(
            @PathVariable String id,
            @RequestBody UpdateActivityRequest request) {
        activityService.updateActivity(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/{id}")
    @PreAuthorizeUser
    ResponseEntity<SimpleResponse> deleteActivity(
            @PathVariable String id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
