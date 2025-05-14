package com.stride.tracking.coreservice.utils;

import com.stride.tracking.coreservice.constant.ProgressTimeFrame;

import java.time.*;
import java.util.Calendar;
import java.util.Date;

public class ProgressTimeFrameHelper {
    private ProgressTimeFrameHelper() {
    }

    public static Calendar getAuditStartCalendar(ProgressTimeFrame timeFrame, ZoneId zoneId) {
        Instant instant = resolveStartDate(
                new Date().toInstant(),
                timeFrame,
                zoneId
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(instant));

        return calendar;
    }

    private static Instant resolveStartDate(
            Instant date,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        Instant instant = InstantUtils.calculateStartDateInstant(date, zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);

        ZonedDateTime result = switch (timeFrame) {
            case WEEK -> zonedDateTime.minusWeeks(1).plusDays(1);
            case MONTH -> zonedDateTime.minusMonths(1).plusDays(1);
            case THREE_MONTHS -> zonedDateTime.minusMonths(3).plusDays(1);
            case SIX_MONTHS -> zonedDateTime.minusMonths(6).plusDays(1);
            case YEAR_TO_DATE -> ZonedDateTime.of(
                    LocalDate.of(zonedDateTime.getYear(), 1, 1),
                    LocalTime.MIN,
                    zoneId
            );
            case YEAR -> zonedDateTime.minusYears(1).plusDays(1);
        };

        return result.toInstant();
    }
}
