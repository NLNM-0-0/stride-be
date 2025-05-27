package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListWithMetadataResponse;
import com.stride.tracking.dto.traininglog.request.TrainingLogFilter;
import com.stride.tracking.dto.traininglog.response.TrainingLogMetadata;
import com.stride.tracking.dto.traininglog.response.TrainingLogResponse;

import java.time.ZoneId;

public interface TrainingLogService {
    ListWithMetadataResponse<TrainingLogResponse, TrainingLogFilter, TrainingLogMetadata> getTrainingLogs(
            TrainingLogFilter filter,
            ZoneId zoneId
    );
}
