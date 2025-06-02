package com.stride.tracking.coreservice.utils;

import java.util.Map;

public class JsonHelper {
    private JsonHelper() {
    }

    public static Object getNestedValue(Object current, String... keys) {
        for (String key : keys) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else if (current instanceof java.util.List) {
                int index = Integer.parseInt(key);
                current = ((java.util.List<?>) current).get(index);
            } else {
                return null;
            }
        }
        return current;
    }
}
