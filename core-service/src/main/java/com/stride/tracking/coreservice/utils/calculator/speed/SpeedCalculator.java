package com.stride.tracking.coreservice.utils.calculator.speed;

import com.stride.tracking.coreservice.constant.RoundRules;
import com.stride.tracking.coreservice.utils.GeometryUtils;
import com.stride.tracking.coreservice.utils.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpeedCalculator {
    public SpeedCalculatorResult calculate(List<double[]> coordinates, List<Long> timestamps) {
        List<Double> distances = new ArrayList<>(List.of(0.0));
        List<Double> speeds = new ArrayList<>(List.of(0.0));
        double maxSpeed = 0.0;

        if (coordinates == null || coordinates.size() < 2) {
            return new SpeedCalculatorResult(distances, speeds, maxSpeed);
        }

        for (int i = 1; i < coordinates.size(); i++) {
            double[] prev = coordinates.get(i - 1);
            double[] curr = coordinates.get(i);

            double lat1 = prev[0];
            double lon1 = prev[1];
            double lat2 = curr[0];
            double lon2 = curr[1];

            double distance = GeometryUtils.distanceToPoint(lat1, lon1, lat2, lon2);
            long secondDiff = (timestamps.get(i) - timestamps.get(i - 1)) / 1000;

            double speedMs = distance / secondDiff;

            double speedKms = GeometryUtils.mToKm(speedMs);
            speedKms = NumberUtils.round(
                    speedKms,
                    RoundRules.SPEED.getValue()
            );

            speeds.add(speedKms);
            distances.add(distances.get(distances.size() - 1) + distance / 1000);

            if (speedKms > maxSpeed) {
                maxSpeed = speedKms;
            }
        }

        return new SpeedCalculatorResult(distances, speeds, maxSpeed);
    }
}
