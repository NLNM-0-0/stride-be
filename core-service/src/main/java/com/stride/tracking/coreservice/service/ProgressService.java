package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.coreservice.dto.progress.request.GetProgressActivityRequest;
import com.stride.tracking.coreservice.dto.progress.request.ProgressFilter;
import com.stride.tracking.coreservice.dto.progress.response.GetProgressActivityResponse;
import com.stride.tracking.coreservice.dto.progress.response.ProgressResponse;
import com.stride.tracking.coreservice.dto.progress.response.ProgressShortResponse;

import java.time.ZoneId;

public interface ProgressService {
    SimpleListResponse<ProgressShortResponse> getProgress(
            ZoneId zoneId,
            ProgressFilter filter
    );

    SimpleListResponse<ProgressResponse> getProgress(
            ZoneId zoneId
    );

    GetProgressActivityResponse getProgressActivity(
            GetProgressActivityRequest request
    );
}
