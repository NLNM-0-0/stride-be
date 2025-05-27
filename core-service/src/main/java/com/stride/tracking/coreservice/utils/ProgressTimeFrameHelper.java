package com.stride.tracking.coreservice.utils;

import com.stride.tracking.dto.progress.ProgressTimeFrame;

import java.time.*;

public class ProgressTimeFrameHelper {
    private ProgressTimeFrameHelper() {
    }

    public static Instant getAuditStartInstant(ZoneId zoneId) {
        return getAuditStartInstant(ProgressTimeFrame.YEAR, zoneId);
    }

    public static Instant getAuditStartInstant(
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        Instant instant = DateUtils.toStartOfDayInstant(Instant.now(), zoneId);
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

    public static Instant resolveStartDate(
            Instant date,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        Instant instant = DateUtils.toStartOfDayInstant(date, zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);

        return switch (timeFrame.getCountType()) {
            case DAILY -> instant;
            case WEEKLY -> zonedDateTime
                    .with(DayOfWeek.MONDAY)
                    .toLocalDate()
                    .atStartOfDay(zoneId)
                    .toInstant();
        };
    }

    public static Instant resolveEndDate(
            Instant start,
            ProgressTimeFrame timeFrame,
            ZoneId zoneId
    ) {
        Instant instant = DateUtils.toEndOfDayInstant(start, zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);

        return switch (timeFrame.getCountType()) {
            case DAILY -> instant;
            case WEEKLY -> zonedDateTime
                    .with(DayOfWeek.SUNDAY)
                    .toLocalDate()
                    .atTime(LocalTime.MAX)
                    .atZone(zoneId)
                    .toInstant();
        };
    }
}
