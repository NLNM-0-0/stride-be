package com.stride.tracking.coreservice.utils.calculator;

import com.stride.tracking.coreservice.constant.RoundRules;
import com.stride.tracking.coreservice.utils.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CarbonSavedCalculator {

    @Value("${app.calories.carbon-saved-per-km}")
    private double carbonSavedPerKm;

    public double calculate(double distanceInKm) {
        double carbonSaved = carbonSavedPerKm * distanceInKm;
        return NumberUtils.round(carbonSaved, RoundRules.CARBON_SAVED.getValue());
    }
}
