package com.stride.tracking.coreservice.utils;

import com.stride.tracking.core.dto.goal.GoalTimeFrame;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class GoalTimeFrameHelper {
    private GoalTimeFrameHelper() {}

    public static Calendar getAuditStartCalendar(GoalTimeFrame timeFrame, ZoneId zoneId) {
        Calendar calendar = getCalendar(timeFrame, zoneId);

        int numberHistories = timeFrame.getNumberHistories() - 1;

        switch (timeFrame) {
            case WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, -numberHistories);
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, -numberHistories);
                break;
            case ANNUALLY:
                calendar.add(Calendar.YEAR, -numberHistories);
                break;
        }

        return calendar;
    }

    public static Calendar getCalendar(GoalTimeFrame timeFrame, ZoneId zoneId) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId));
        ZonedDateTime zonedNow = ZonedDateTime.now(zoneId);

        calendar.setTime(Date.from(zonedNow.toInstant()));

        switch (timeFrame) {
            case WEEKLY -> calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            case MONTHLY -> calendar.set(Calendar.DAY_OF_MONTH, 1);
            case ANNUALLY -> {
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            }
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }


    public static List<Date> generateExpectedDates(GoalTimeFrame timeFrame, ZoneId zoneId) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = getAuditStartCalendar(timeFrame, zoneId);

        for (int i = 0; i < timeFrame.getNumberHistories(); i++) {
            dates.add(calendar.getTime());
            switch (timeFrame) {
                case WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1);
                case MONTHLY -> calendar.add(Calendar.MONTH, 1);
                case ANNUALLY -> calendar.add(Calendar.YEAR, 1);
            }
        }

        return dates;
    }

    public static String formatDateKey(Date date, GoalTimeFrame timeFrame) {
        String pattern;
        switch (timeFrame) {
            case WEEKLY -> pattern = "dd/MM/yyyy";
            case MONTHLY -> pattern = "MM/yyyy";
            case ANNUALLY -> pattern = "yyyy";
            default -> throw new IllegalArgumentException("Unknown GoalTimeFrame: " + timeFrame);
        }
        return new SimpleDateFormat(pattern).format(date);
    }
}
