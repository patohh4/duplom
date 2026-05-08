package com.mindoc.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date operations
 */
public class DateUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public static String formatDate(LocalDate date) {
        return date.format(FORMATTER);
    }
    
    public static LocalDate parseDate(String dateString) throws DateTimeParseException {
        return LocalDate.parse(dateString, FORMATTER);
    }
    
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }
    
    public static LocalDate weekAgo() {
        return LocalDate.now().minusDays(7);
    }
    
    public static LocalDate monthAgo() {
        return LocalDate.now().minusMonths(1);
    }
    
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }
    
    public static boolean isYesterday(LocalDate date) {
        return date.equals(LocalDate.now().minusDays(1));
    }
    
    public static long getDaysBetween(LocalDate startDate, LocalDate endDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    public static String getDayName(LocalDate date) {
        return date.getDayOfWeek().toString();
    }
    
    public static String getMonthName(LocalDate date) {
        return date.getMonth().toString();
    }
}
