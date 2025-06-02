package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.sport.request.CreateSportRequest;
import com.stride.tracking.core.dto.sport.request.SportFilter;
import com.stride.tracking.core.dto.sport.request.UpdateSportRequest;
import com.stride.tracking.core.dto.sport.response.SportResponse;
import com.stride.tracking.core.dto.sport.response.SportShortResponse;

public interface SportService {
    ListResponse<SportResponse, SportFilter> getSports(
            AppPageRequest page,
            SportFilter filter
    );
    SimpleListResponse<SportShortResponse> getSports();
    SportResponse createSport(CreateSportRequest request);
    void updateSport(String sportId, UpdateSportRequest request);
    void deleteSport(String sportId);
}
