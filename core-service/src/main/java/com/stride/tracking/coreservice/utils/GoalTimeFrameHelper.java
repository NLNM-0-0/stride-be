package com.stride.tracking.coreservice.utils;

import com.stride.tracking.coreservice.constant.GoalTimeFrame;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GoalTimeFrameHelper {
    private GoalTimeFrameHelper() {}

    public static Calendar getAuditStartCalendar(GoalTimeFrame timeFrame) {
        Calendar calendar = getCalendar(timeFrame);

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

    public static Calendar getCalendar(GoalTimeFrame timeFrame) {
        Calendar calendar = Calendar.getInstance();

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


    public static List<Date> generateExpectedDates(GoalTimeFrame timeFrame) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = getAuditStartCalendar(timeFrame);

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
