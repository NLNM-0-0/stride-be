package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.dto.progress.request.GetProgressActivityRequest;
import com.stride.tracking.dto.progress.request.ProgressFilter;
import com.stride.tracking.dto.progress.response.GetProgressActivityResponse;
import com.stride.tracking.dto.progress.response.ProgressDetailResponse;
import com.stride.tracking.dto.progress.response.ProgressResponse;

import java.time.ZoneId;

public interface ProgressService {
    ProgressDetailResponse getProgress(
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
