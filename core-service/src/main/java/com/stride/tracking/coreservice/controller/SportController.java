package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeAdmin;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.sport.request.CreateSportRequest;
import com.stride.tracking.core.dto.sport.request.SportFilter;
import com.stride.tracking.core.dto.sport.request.UpdateSportRequest;
import com.stride.tracking.core.dto.sport.response.SportResponse;
import com.stride.tracking.core.dto.sport.response.SportShortResponse;
import com.stride.tracking.coreservice.service.SportService;
import jakarta.annotation.security.PermitAll;
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

    @GetMapping("/manage")
    @PreAuthorizeAdmin
    ResponseEntity<ListResponse<SportResponse, SportFilter>> getSports(
            @Valid AppPageRequest page,
            @Valid SportFilter filter) {
        return ResponseEntity.ok(sportService.getSports(page, filter));
    }

    @GetMapping("/all")
    @PermitAll
    ResponseEntity<SimpleListResponse<SportShortResponse>> getSports() {
        return ResponseEntity.ok(sportService.getSports());
    }

    @PostMapping("/manage")
    @PreAuthorizeAdmin
    ResponseEntity<SportResponse> createSport(
            @Valid @RequestBody CreateSportRequest request
    ) {
        SportResponse response = sportService.createSport(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/manage/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> updateSport(
            @PathVariable String id,
            @Valid @RequestBody UpdateSportRequest request
    ) {
        sportService.updateSport(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/manage/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> deleteSport(
            @PathVariable String id) {
        sportService.deleteSport(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
