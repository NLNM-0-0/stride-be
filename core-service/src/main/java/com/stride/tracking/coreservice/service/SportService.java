package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.dto.sport.request.CreateSportRequest;
import com.stride.tracking.dto.sport.request.SportFilter;
import com.stride.tracking.dto.sport.request.UpdateSportRequest;
import com.stride.tracking.dto.sport.response.SportResponse;
import com.stride.tracking.dto.sport.response.SportWithColorResponse;

public interface SportService {
    ListResponse<SportResponse, SportFilter> getSports(
            AppPageRequest page,
            SportFilter filter
    );
    SimpleListResponse<SportWithColorResponse> getSports();
    SportResponse createSport(CreateSportRequest request);
    void updateSport(String sportId, UpdateSportRequest request);
    void deleteSport(String sportId);
}
