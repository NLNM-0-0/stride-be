package com.stride.tracking.coreservice.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    private ListUtils() {}

    public static <T> List<T> minimized(List<T> list, int maxPoints) {
        int size = list.size();
        if (size <= maxPoints) {
            return list;
        }

        List<T> result = new ArrayList<>(maxPoints);
        double step = (double) size / maxPoints;

        for (int i = 0; i < maxPoints; i++) {
            int index = (int) Math.floor(i * step);
            result.add(list.get(index));
        }

        return result;
    }
}
