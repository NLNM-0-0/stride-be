package com.stride.tracking.coreservice.utils.calculator.heartrate;

import com.stride.tracking.dto.constant.HeartRateZone;

import java.util.Map;

public record HeartRateCalculatorResult(
        Double avgHeartRate,
        Double maxHeartRate,
        Map<HeartRateZone, Integer> heartRateZones) {
}
