package com.stride.tracking.coreservice.utils;

import java.time.*;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private DateUtils() {
    }

    public static Date toStartDate(Instant instant, ZoneId zoneId) {
        return Date.from(
                toStartOfDayInstant(
                        instant,
                        zoneId
                )
        );
    }

    public static Date toEndDate(Instant instant, ZoneId zoneId) {
        return Date.from(
                toEndOfDayInstant(
                        instant,
                        zoneId
                )
        );
    }

    public static Long getStartOfWeekDate(ZoneId zoneId) {
        LocalDate localDate = Instant
                .now()
                .atZone(zoneId)
                .toLocalDate();

        LocalDate startOfWeek = localDate.with(DayOfWeek.MONDAY);

        return startOfWeek
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli();
    }

    public static Long getEndOfWeekDate(ZoneId zoneId) {
        LocalDate localDate = Instant
                .now()
                .atZone(zoneId)
                .toLocalDate();

        LocalDate endOfWeek = localDate.with(DayOfWeek.SUNDAY);

        return endOfWeek
                .atTime(LocalTime.MAX)
                .atZone(zoneId)
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli();
    }

    public static Date toDate(Instant instant) {
        return Date.from(instant);
    }

    public static Date toDate(Long milliseconds) {
        return toDate(toInstant(milliseconds));
    }

    public static Instant toInstant(Long milliseconds) {
        return Instant.ofEpochMilli(milliseconds);
    }

    public static Calendar toStartOfDayCalendar(Long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDate(milliseconds));

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    public static Instant toStartOfDayInstant(
            Instant instant,
            ZoneId zoneId
    ) {
        ZonedDateTime zonedDateTime = instant
                .atZone(zoneId)
                .toLocalDate()
                .atStartOfDay(zoneId);

        return zonedDateTime.toInstant();
    }

    public static Instant toEndOfDayInstant(
            Instant instant,
            ZoneId zoneId
    ) {
        ZonedDateTime zonedDateTime = instant
                .atZone(zoneId)
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(zoneId);

        return zonedDateTime.toInstant();
    }
}
