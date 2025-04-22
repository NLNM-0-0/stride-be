package com.stride.tracking.coreservice.utils.calculator.elevation;

import java.util.List;

public record ElevationCalculatorResult(List<Integer> elevations, Integer elevationGain, Integer maxElevation) {
}