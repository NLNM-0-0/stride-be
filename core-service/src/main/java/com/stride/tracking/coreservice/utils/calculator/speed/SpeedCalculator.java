package com.stride.tracking.coreservice.utils.calculator.speed;

import com.stride.tracking.coreservice.utils.GeometryUtils;
import com.stride.tracking.coreservice.utils.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpeedCalculator {
    public SpeedCalculatorResult calculate(List<List<Double>> coordinates, List<Long> timestamps) {
        List<Double> speeds = new ArrayList<>();
        double maxSpeed = 0.0;

        if (coordinates == null || coordinates.size() < 2) {
            return new SpeedCalculatorResult(speeds, maxSpeed);
        }

        for (int i = 1; i < coordinates.size(); i++) {
            List<Double> prev = coordinates.get(i - 1);
            List<Double> curr = coordinates.get(i);

            double lat1 = prev.get(0);
            double lon1 = prev.get(1);
            double lat2 = curr.get(0);
            double lon2 = curr.get(1);

            double distance = GeometryUtils.distanceToPoint(lat1, lon1, lat2, lon2);
            long secondDiff = (timestamps.get(i) - timestamps.get(i - 1)) / 1000;

            double speedMs = distance / secondDiff;

            double speedKms = GeometryUtils.mToKm(speedMs);
            speedKms = NumberUtils.round(speedKms, 5);

            speeds.add(speedKms);

            if (speedKms > maxSpeed) {
                maxSpeed = speedKms;
            }
        }

        return new SpeedCalculatorResult(speeds, maxSpeed);
    }
}
