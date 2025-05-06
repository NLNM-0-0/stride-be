package com.stride.tracking.coreservice.utils.calculator.heartrate;

import com.stride.tracking.coreservice.model.HeartRateZoneValue;

import java.util.List;

public record HeartRateCalculatorResult(
        Double avgHeartRate,
        Double maxHeartRate,
        List<HeartRateZoneValue> heartRateZones) {
}
