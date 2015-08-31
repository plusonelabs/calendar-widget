package com.plusonelabs.calendar;

import android.content.Context;
import android.text.format.DateUtils;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private static final String TWELVE = "12";
    private static final String NOON_AM = "12:00 AM";
    private static final String EMPTY_STRING = "";
    private static final String COMMA_SPACE = ", ";
    private static volatile DateTime mNow = null;
    private static volatile DateTime mNowSetAt = DateTime.now();

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
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
    }

    private static String createDateString(Context context, Date date, String prefix) {
        return prefix + COMMA_SPACE + DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_DATE);
    }

    public static void setNow(DateTime now) {
        mNowSetAt = DateTime.now();
        mNow = now;
    }

    /**
     * Usually returns real "now", but may be #setNow to some other time for testing purposes
     */
    public static DateTime now() {
        DateTime nowSetAt;
        DateTime now;
        do {
            nowSetAt = mNowSetAt;
            now = mNow;
        } while (nowSetAt != mNowSetAt); // Ensure concurrent consistency
        if (now == null) {
            return DateTime.now();
        } else {
            long diffL = DateTime.now().getMillis() - nowSetAt.getMillis();
            int diff = 0;
            if (diffL > 0 && diffL < Integer.MAX_VALUE) {
                diff = (int) diffL;
            }
            return new DateTime(now).plusMillis(diff);
        }
    }
}
