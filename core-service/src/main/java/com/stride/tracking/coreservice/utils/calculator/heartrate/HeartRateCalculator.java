package com.stride.tracking.coreservice.utils.calculator.heartrate;

import com.stride.tracking.coreservice.model.HeartRateZoneValue;
import com.stride.tracking.coreservice.utils.NumberUtils;
import com.stride.tracking.dto.user.HeartRateZone;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HeartRateCalculator {

    public HeartRateCalculatorResult calculate(
            List<Integer> heartRates,
            Map<HeartRateZone, Integer> userHRZones
    ) {
        if (heartRates == null || heartRates.isEmpty()) {
            return new HeartRateCalculatorResult(0.0, 0.0, List.of());
        }

        double sum = 0;
        double max = Double.MIN_VALUE;
        Map<HeartRateZone, Integer> zoneCounts = new EnumMap<>(HeartRateZone.class);
        for (HeartRateZone zone : HeartRateZone.values()) {
            zoneCounts.put(zone, 0);
        }

        for (int hr : heartRates) {
            sum += hr;
            if (hr > max) {
                max = hr;
            }

            HeartRateZone zone = classifyZone(hr, userHRZones);
            zoneCounts.put(zone, zoneCounts.get(zone) + 1);
        }

        double avg = NumberUtils.round(sum / heartRates.size(), 1);

        List<HeartRateZoneValue> heartRateZoneValues = new ArrayList<>();
        Map<HeartRateZone, Integer> zoneThresholds = new EnumMap<>(userHRZones); // defensive copy

        // Sort zones to get correct min/max ranges
        List<HeartRateZone> sortedZones = Arrays.asList(HeartRateZone.values());

        for (int i = 0; i < sortedZones.size(); i++) {
            HeartRateZone zone = sortedZones.get(i);
            int maxHr = zoneThresholds.getOrDefault(zone, Integer.MAX_VALUE);
            int minHr = (i == 0) ? 0 : zoneThresholds.getOrDefault(sortedZones.get(i - 1), 0) + 1;

            heartRateZoneValues.add(HeartRateZoneValue.builder()
                    .zone(zone)
                    .min(minHr)
                    .max(maxHr)
                    .value(zoneCounts.getOrDefault(zone, 0))
                    .build());
        }

        return new HeartRateCalculatorResult(avg, max, heartRateZoneValues);
    }

    private HeartRateZone classifyZone(int hr, Map<HeartRateZone, Integer> userHRZones) {
        for (HeartRateZone zone : HeartRateZone.values()) {
            int maxHrForZone = userHRZones.getOrDefault(zone, Integer.MAX_VALUE);
            if (hr <= maxHrForZone) {
                return zone;
            }
        }
        return HeartRateZone.ZONE5;
    }
}
