package com.stride.tracking.coreservice.utils;

import java.time.*;

public class InstantUtils {
    private InstantUtils() {}

    public static Instant calculateStartDateInstant(
            Instant date,
            ZoneId zoneId
    ) {
        ZonedDateTime zonedDateTime = date.atZone(zoneId);

        zonedDateTime = zonedDateTime.toLocalDate().atStartOfDay(zonedDateTime.getZone());

        return zonedDateTime.toInstant();
    }
}
