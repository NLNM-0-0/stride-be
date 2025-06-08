package com.stride.tracking.coreservice.utils.calculator.speed;

import com.stride.tracking.core.dto.activity.request.CoordinateRequest;
import com.stride.tracking.coreservice.constant.ActivityConst;
import com.stride.tracking.coreservice.constant.RoundRules;
import com.stride.tracking.coreservice.utils.GeometryUtils;
import com.stride.tracking.coreservice.utils.ListUtils;
import com.stride.tracking.coreservice.utils.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpeedCalculator {
    public SpeedCalculatorResult calculate(
            List<CoordinateRequest> coordinateRequests
    ) {
        List<Double> distances = new ArrayList<>();
        List<Double> speeds = new ArrayList<>();
        List<Long> timeStamps = new ArrayList<>();
        double maxSpeed = 0.0;
        double sumSpeed = 0.0;

        if (coordinateRequests == null || coordinateRequests.size() < 2) {
            return new SpeedCalculatorResult(distances, sumSpeed, maxSpeed, speeds, timeStamps, new ArrayList<>());
        }

        int size = coordinateRequests.size();
        for (int i = 1; i < size; i++) {
            List<Double> prevCoordinate = coordinateRequests.get(i - 1).getCoordinate();
            List<Double> currCoordinate = coordinateRequests.get(i).getCoordinate();

            double lat1 = prevCoordinate.get(0);
            double lon1 = prevCoordinate.get(1);
            double lat2 = currCoordinate.get(0);
            double lon2 = currCoordinate.get(1);

            double distance = GeometryUtils.distanceToPoint(lat1, lon1, lat2, lon2);

            Long prevTimestamp = coordinateRequests.get(i - 1).getTimestamp();
            Long currTimestamp = coordinateRequests.get(i).getTimestamp();
            timeStamps.add(currTimestamp);
            double secondDiff = (currTimestamp - prevTimestamp) / 1000.0;

            double speedMs = secondDiff == 0 ? 0 : distance / secondDiff;

            double speedKms = GeometryUtils.mToKm(speedMs);
            speedKms = NumberUtils.round(
                    speedKms,
                    RoundRules.SPEED.getValue()
            );

            sumSpeed += speedKms;

            speeds.add(speedKms);

            if (!distances.isEmpty()) {
                distances.add(distances.get(distances.size() - 1) + distance / 1000);
            } else {
                distances.add(distance / 1000);
            }


            if (speedKms > maxSpeed) {
                maxSpeed = speedKms;
            }
        }

        Double avgSpeed = sumSpeed / speeds.size();

        List<Integer> indicates = ListUtils.minimizedIndices(speeds, ActivityConst.NUMBER_CHART_POINTS);

        distances = indicates.stream().map(distances::get).toList();
        speeds = indicates.stream().map(speeds::get).toList();
        timeStamps = indicates.stream().map(timeStamps::get).toList();

        return new SpeedCalculatorResult(
                distances,
                avgSpeed,
                maxSpeed,
                speeds,
                timeStamps,
                indicates
        );
    }
}
