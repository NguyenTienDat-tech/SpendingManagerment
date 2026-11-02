package com.example.spendingmanagerment.core.utils;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateUtils {
    private DateUtils() { }
    public static LocalDate periodStart(LocalDate date, boolean monthly) {
        return monthly ? date.withDayOfMonth(1) : date;
    }
    public static LocalDate periodEnd(LocalDate date, boolean monthly) {
        LocalDate start = periodStart(date, monthly);
        return monthly ? start.plusMonths(1) : start.plusDays(1);
    }
    public static long startOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public static LocalDate fromMillis(long millis) {
        return java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
    public static String formatDate(LocalDate date, boolean monthly, Locale locale) {
        return date.format(DateTimeFormatter.ofPattern(monthly ? "MM/yyyy" : "dd/MM/yyyy", locale));
    }
    public static String formatAmount(long amount, Locale locale) {
        return NumberFormat.getIntegerInstance(locale).format(amount) + " ₫";
    }
}
