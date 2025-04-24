package com.stride.tracking.coreservice.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {
    private NumberUtils() {}

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Places must be non-negative");

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
