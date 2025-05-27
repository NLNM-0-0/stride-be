package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.constants.CustomHeaders;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.coreservice.service.GoalService;
import com.stride.tracking.dto.goal.request.CreateGoalRequest;
import com.stride.tracking.dto.goal.request.UpdateGoalRequest;
import com.stride.tracking.dto.goal.response.CreateGoalResponse;
import com.stride.tracking.dto.goal.response.GoalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @GetMapping("/profile")
    ResponseEntity<SimpleListResponse<GoalResponse>> getGoals(
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone
    ) {
        ZoneId zoneId = ZoneId.of(timezone);
        return ResponseEntity.ok(goalService.getUserGoals(zoneId));
    }

    @PostMapping
    ResponseEntity<CreateGoalResponse> createGoal(@RequestBody CreateGoalRequest request) {
        CreateGoalResponse response = goalService.createGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<SimpleResponse> updateGoal(
            @PathVariable String id,
            @RequestHeader(value = CustomHeaders.X_USER_TIMEZONE, defaultValue = "UTC") String timezone,
            @RequestBody UpdateGoalRequest request) {
        ZoneId zoneId = ZoneId.of(timezone);

        goalService.updateGoal(id, zoneId, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<SimpleResponse> deleteGoal(
            @PathVariable String id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
