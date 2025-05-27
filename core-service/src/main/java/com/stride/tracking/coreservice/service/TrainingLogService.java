package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListWithoutPagingResponse;
import com.stride.tracking.dto.traininglog.request.TrainingLogFilter;
import com.stride.tracking.dto.traininglog.response.TrainingLogResponse;

import java.time.ZoneId;

public interface TrainingLogService {
    ListWithoutPagingResponse<TrainingLogResponse, TrainingLogFilter> getTrainingLogs(
            TrainingLogFilter filter,
            ZoneId zoneId
    );
}
