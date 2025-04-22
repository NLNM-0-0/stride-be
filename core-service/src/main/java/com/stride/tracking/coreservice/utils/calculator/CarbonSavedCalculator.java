package com.stride.tracking.coreservice.utils.calculator;

import com.stride.tracking.coreservice.utils.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CarbonSavedCalculator {

    private final double carbonSavedPerKm;

    public CarbonSavedCalculator(@Value("${app.calories.carbon-saved-per-km}") double carbonSavedPerKm) {
        this.carbonSavedPerKm = carbonSavedPerKm;
    }

    public double calculate(double distanceInKm) {
        double carbonSaved = carbonSavedPerKm * distanceInKm;
        return NumberUtils.round(carbonSaved, 1);
    }
}
