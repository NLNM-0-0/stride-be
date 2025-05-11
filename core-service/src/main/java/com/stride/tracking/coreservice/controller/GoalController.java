package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.coreservice.dto.goal.request.CreateGoalRequest;
import com.stride.tracking.coreservice.dto.goal.request.UpdateGoalRequest;
import com.stride.tracking.coreservice.dto.goal.response.CreateGoalResponse;
import com.stride.tracking.coreservice.dto.goal.response.GoalResponse;
import com.stride.tracking.coreservice.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @GetMapping("/profile")
    ResponseEntity<SimpleListResponse<GoalResponse>> getGoals() {
        return ResponseEntity.ok(goalService.getUserGoals());
    }

    @PostMapping
    ResponseEntity<CreateGoalResponse> createGoal(@RequestBody CreateGoalRequest request) {
        CreateGoalResponse response = goalService.createGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<SimpleResponse> updateActivity(
            @PathVariable String id,
            @RequestBody UpdateGoalRequest request) {
        goalService.updateGoal(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<SimpleResponse> deleteGoal(
            @PathVariable String id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
