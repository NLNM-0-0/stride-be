package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.dto.sport.request.CreateSportRequest;
import com.stride.tracking.dto.sport.request.SportFilter;
import com.stride.tracking.dto.sport.request.UpdateSportRequest;
import com.stride.tracking.dto.sport.response.SportResponse;
import com.stride.tracking.coreservice.service.SportService;
import com.stride.tracking.dto.sport.response.SportShortResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sports")
@RequiredArgsConstructor
public class SportController {
    private final SportService sportService;

    @GetMapping
    ResponseEntity<ListResponse<SportResponse, SportFilter>> getSports(
            @Valid AppPageRequest page,
            @Valid SportFilter filter) {
        return ResponseEntity.ok(sportService.getSports(page, filter));
    }

    @GetMapping("/all")
    ResponseEntity<SimpleListResponse<SportShortResponse>> getSports() {
        return ResponseEntity.ok(sportService.getSports());
    }

    @PostMapping
    ResponseEntity<SportResponse> createSport(@RequestBody CreateSportRequest request) {
        SportResponse response = sportService.createSport(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<SimpleResponse> updateSport(
            @PathVariable String id,
            @RequestBody UpdateSportRequest request) {
        sportService.updateSport(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<SimpleResponse> deleteSport(
            @PathVariable String id) {
        sportService.deleteSport(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
