package com.stride.tracking.coreservice.utils.calculator.heartrate;

import com.stride.tracking.coreservice.utils.NumberUtils;
import com.stride.tracking.dto.constant.HeartRateZone;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class HeartRateCalculator {
    public HeartRateCalculatorResult calculate(
            List<Integer> heartRates,
            Map<HeartRateZone, Integer> userHRZones
    ) {
        if (heartRates == null || heartRates.isEmpty()) {
            return new HeartRateCalculatorResult(0.0, 0.0, new EnumMap<>(HeartRateZone.class));
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

        return new HeartRateCalculatorResult(avg, max, zoneCounts);
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
