package com.plusonelabs.calendar;

import android.content.Context;
import android.text.format.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private static final String TWELVE = "12";
    private static final String NOON_AM = "12:00 AM";
    private static final String EMPTY_STRING = "";
    private static final String COMMA_SPACE = ", ";
    private static final String PRE_DAYS = " (+";
    private static final String POST_DAYS = ")";

    public static boolean isMidnight(DateTime date) {
        return date.isEqual(date.withTimeAtStartOfDay());
    }

    public static boolean hasAmPmClock(Locale locale) {
        DateFormat stdFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        DateFormat localeFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
        String midnight = EMPTY_STRING;
        try {
            midnight = localeFormat.format(stdFormat.parse(NOON_AM));
        } catch (ParseException ignore) {
            // we ignore this exception deliberately
        }
        return midnight.contains(TWELVE);
    }

    public static String createDateString(Context context, DateTime dateTime) {
        DateTime timeAtStartOfToday = DateTime.now().withTimeAtStartOfDay();
        if (dateTime.withTimeAtStartOfDay().isEqual(timeAtStartOfToday)) {
            return createDateString(context, dateTime.toDate(), context.getString(R.string.today));
        } else if (dateTime.withTimeAtStartOfDay().isEqual(timeAtStartOfToday.plusDays(1))) {
            return createDateString(context, dateTime.toDate(), context.getString(R.string.tomorrow));
        }
        return DateUtils.formatDateTime(context, dateTime.toDate().getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY) + PRE_DAYS + daysBetween(DateTime.now().toDate(), dateTime.toDate()) + POST_DAYS;
    }

    private static String createDateString(Context context, Date date, String prefix) {
        return prefix + COMMA_SPACE + DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_DATE);
    }
    
    private static String daysBetween(Date start, Date end) {
        return String.valueOf( Days.daysBetween( start.toDateMidnight(), end.toDateMidnight() ).getDays() );
    }
}
