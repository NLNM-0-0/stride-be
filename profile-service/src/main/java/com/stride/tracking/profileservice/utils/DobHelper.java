package com.stride.tracking.profileservice.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DobHelper {
    private DobHelper() {}

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static int getAge(String dob) {
        LocalDate birthDate = LocalDate.parse(dob, DATE_FORMATTER);
        LocalDate currentDate = LocalDate.now();

        return (int) ChronoUnit.YEARS.between(birthDate, currentDate);
    }
}
