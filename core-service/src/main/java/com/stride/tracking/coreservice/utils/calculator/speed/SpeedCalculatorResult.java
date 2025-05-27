package com.stride.tracking.coreservice.utils.calculator.speed;

import java.util.List;

public record SpeedCalculatorResult(List<Double> distances, Double avgSpeed, Double maxSpeed, List<Double> speeds) {}