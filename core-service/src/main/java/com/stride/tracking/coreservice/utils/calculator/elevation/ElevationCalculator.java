package com.stride.tracking.coreservice.utils.calculator.elevation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ElevationCalculator {
    public ElevationCalculatorResult calculate(List<Integer> elevations) {
        if (elevations == null || elevations.size() < 2) {
            return new ElevationCalculatorResult(elevations, 0, Integer.MIN_VALUE);
        }

        int totalElevationGain = 0;
        int maxElevation = Integer.MIN_VALUE;

        for (int i = 1; i < elevations.size(); i++) {
            int prevElevation = elevations.get(i - 1);
            int currElevation = elevations.get(i);

            int elevationDiff = currElevation - prevElevation;
            if (elevationDiff > 0) {
                totalElevationGain += elevationDiff;
            }

            if (currElevation > maxElevation) {
                maxElevation = currElevation;
            }
        }

        return new ElevationCalculatorResult(
                elevations,
                totalElevationGain,
                maxElevation
        );
    }
}