package com.stride.tracking.metricservice.service;

import com.stride.tracking.commons.dto.ListWithMetadataResponse;
import com.stride.tracking.metric.dto.traininglog.request.TrainingLogFilter;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogMetadata;
import com.stride.tracking.metric.dto.traininglog.response.TrainingLogResponse;

import java.time.ZoneId;

public interface TrainingLogService {
    ListWithMetadataResponse<TrainingLogResponse, TrainingLogFilter, TrainingLogMetadata> getTrainingLogs(
            ZoneId zoneId,
            TrainingLogFilter filter
    );
}
