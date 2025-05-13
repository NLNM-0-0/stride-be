package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.constants.CustomHeaders;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.coreservice.service.ActivityService;
import com.stride.tracking.dto.activity.request.ActivityFilter;
import com.stride.tracking.dto.activity.request.CreateActivityRequest;
import com.stride.tracking.dto.activity.request.UpdateActivityRequest;
import com.stride.tracking.dto.activity.response.ActivityResponse;
import com.stride.tracking.dto.activity.response.ActivityShortResponse;
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
    ResponseEntity<ListResponse<ActivityShortResponse, ActivityFilter>> getActivitiesOfUser(
            @Valid AppPageRequest page) {
        return ResponseEntity.ok(activityService.getActivitiesOfUser(page));
    }

    @GetMapping("/{id}")
    ResponseEntity<ActivityResponse> getActivity(
            @PathVariable String id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @PostMapping
    ResponseEntity<ActivityShortResponse> createActivity(
            @RequestBody CreateActivityRequest request,
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone
    ) {
        ZoneId zoneId = ZoneId.of(timezone);

        ActivityShortResponse response = activityService.createActivity(zoneId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<SimpleResponse> updateActivity(
            @PathVariable String id,
            @RequestBody UpdateActivityRequest request) {
        activityService.updateActivity(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<SimpleResponse> deleteActivity(
            @PathVariable String id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
