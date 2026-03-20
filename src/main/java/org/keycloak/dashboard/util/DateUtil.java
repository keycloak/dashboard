package org.keycloak.dashboard.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    private static final SimpleDateFormat JSON_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final DateTimeFormatter DATE_STRING_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    private static final SimpleDateFormat TO_MONTH_STRING_FORMATTER = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
    private static final SimpleDateFormat TO_YEAR_STRING_FORMATTER = new SimpleDateFormat("yyyy", Locale.ENGLISH);

    private static final SimpleDateFormat TO_DATE_STRING_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    static {
        JSON_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        TO_MONTH_STRING_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        TO_YEAR_STRING_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        TO_DATE_STRING_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static final java.util.Date MINUS_6_MONTHS = DateUtil.minusMonths(6);
    public static final java.util.Date MINUS_12_MONTHS = DateUtil.minusMonths(12);
    public static final java.util.Date MINUS_18_MONTHS = DateUtil.minusMonths(18);
    public static final java.util.Date MINUS_24_MONTHS = DateUtil.minusMonths(24);
    public static final java.util.Date MINUS_36_MONTHS = DateUtil.minusMonths(36);

    public static final java.util.Date MINUS_7_DAYS = DateUtil.minusdays(7);
    public static final java.util.Date MINUS_30_DAYS = DateUtil.minusdays(30);
    public static final java.util.Date MINUS_90_DAYS = DateUtil.minusdays(90);

    public static final String MINUS_6_MONTHS_STRING = DateUtil.minusMonthsString(6);
    public static final String MINUS_12_MONTHS_STRING = DateUtil.minusMonthsString(12);
    public static final String MINUS_18_MONTHS_STRING = DateUtil.minusMonthsString(18);

    public static final String MINUS_7_DAYS_STRING = DateUtil.minusDaysString(7);
    public static final String MINUS_30_DAYS_STRING = DateUtil.minusDaysString(30);
    public static final String MINUS_90_DAYS_STRING = DateUtil.minusDaysString(90);

    public static java.util.Date minusdays(int days) {
        Calendar cal = getCalendarUTC();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -days);
        return cal.getTime();
    }

    private static Calendar getCalendarUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    public static java.util.Date minusMonths(int months) {
        Calendar cal = getCalendarUTC();
        cal.add(Calendar.MONTH, -months);
        return cal.getTime();
    }

    public static String minusMonthsString(int months) {
        return DATE_STRING_FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC).minusMonths(months));
    }

    public static String minusDaysString(int days) {
        return DATE_STRING_FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC).minusDays(days));
    }

    public static Date fromJson(String date) {
        try {
            return JSON_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String monthString(Date date) {
        return TO_MONTH_STRING_FORMATTER.format(date);
    }

    public static String yearString(Date date) {
        return TO_YEAR_STRING_FORMATTER.format(date);
    }

    public static String toString(java.util.Date date) {
        return TO_DATE_STRING_FORMATTER.format(date);
    }

    public static List<String> monthStrings(int history) {
        List<String> months = new LinkedList<>();
        Calendar now = getCalendarUTC();

        Calendar cal = getCalendarUTC();
        cal.add(Calendar.MONTH, -history);

        while (true) {
            months.add(DateUtil.monthString(cal.getTime()));
            cal.add(Calendar.MONTH, 1);
            if (cal.after(now)) {
                break;
            }
        }

        return months;
    }

}
