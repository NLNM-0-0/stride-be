package com.stride.tracking.profileservice.utils.heartrate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class FoxCalculator implements MaxHearRateCalculator {
    @Override
    public int calculate(int age) {
        return 220 - age;
    }
}
