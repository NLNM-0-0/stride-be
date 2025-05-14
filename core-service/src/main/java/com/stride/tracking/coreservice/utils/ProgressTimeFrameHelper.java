package com.stride.tracking.coreservice.utils;

import com.stride.tracking.coreservice.constant.ProgressTimeFrame;

import java.time.*;
import java.util.Calendar;
import java.util.Date;

public class ProgressTimeFrameHelper {
    private ProgressTimeFrameHelper(){}

    public static Calendar getAuditStartCalendar(ProgressTimeFrame timeFrame, ZoneId zoneId) {
        LocalDate startDate = resolveStartDate(timeFrame, zoneId);
        ZonedDateTime zonedDateTime = startDate.atStartOfDay(zoneId);
        Instant instant = zonedDateTime.toInstant();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(instant));

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    private static LocalDate resolveStartDate(ProgressTimeFrame timeFrame, ZoneId zoneId) {
        LocalDate now = LocalDate.now(zoneId);

        return switch (timeFrame) {
            case WEEK -> now.with(DayOfWeek.MONDAY);
            case MONTH -> now.withDayOfMonth(1);
            case THREE_MONTHS -> now.minusMonths(3).withDayOfMonth(1);
            case SIX_MONTHS -> now.minusMonths(6).withDayOfMonth(1);
            case YEAR_TO_DATE -> LocalDate.of(now.getYear(), 1, 1);
            case YEAR -> now.minusYears(1).plusDays(1);
        };
    }
}
