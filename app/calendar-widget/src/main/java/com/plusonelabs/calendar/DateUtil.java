package com.plusonelabs.calendar;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    private static final String COMMA_SPACE = ", ";
    private static volatile DateTime mNow = null;
    private static volatile DateTime mNowSetAt = DateTime.now();

    public static boolean isMidnight(DateTime date) {
        return date.isEqual(date.withTimeAtStartOfDay());
    }

    public static String createDateString(Context context, DateTime dateTime) {
        DateTime timeAtStartOfToday = DateTime.now().withTimeAtStartOfDay();
        if (CalendarPreferences.getAbbreviateDates(context)) {
            return formatDateTime(context, dateTime,
                    DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE |
                    DateUtils.FORMAT_SHOW_WEEKDAY);
        }
        if (dateTime.withTimeAtStartOfDay().isEqual(timeAtStartOfToday)) {
            return createDateString(context, dateTime, context.getString(R.string.today));
        } else if (dateTime.withTimeAtStartOfDay().isEqual(timeAtStartOfToday.plusDays(1))) {
            return createDateString(context, dateTime, context.getString(R.string.tomorrow));
        }
        return formatDateTime(context, dateTime,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
    }

    private static String createDateString(Context context, DateTime dateTime, String prefix) {
        return prefix + COMMA_SPACE + formatDateTime(context, dateTime, DateUtils.FORMAT_SHOW_DATE);
    }

    public static String formatDateTime(Context context, DateTime dateTime, int flags) {
        return CalendarPreferences.isTimeZoneLocked(context) ?
                formatDateTimeAtTimeZone(context, dateTime, flags, CalendarPreferences.getLockedTimeZoneId(context)) :
                DateUtils.formatDateTime(context, dateTime.getMillis(), flags);
    }

    private static String formatDateTimeAtTimeZone(Context context, DateTime dateTime, int flags, String timeZoneId) {
        return DateUtils.formatDateRange(context,
                new Formatter(new StringBuilder(50), Locale.getDefault()),
                dateTime.getMillis(), dateTime.getMillis(), flags,
                timeZoneId).toString();
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

    public static DateTimeZone getCurrentTimeZone(Context context) {
        DateTimeZone zone = DateTimeZone.forID(TimeZone.getDefault().getID());
        if (CalendarPreferences.isTimeZoneLocked(context)) {
            String lockedTimeZoneId = CalendarPreferences.getLockedTimeZoneId(context);
            try {
                zone = DateTimeZone.forID(lockedTimeZoneId);
            } catch (IllegalArgumentException e) {
                Log.w("getCurrentTimeZone", "The Locked time zone is not recognized: " + lockedTimeZoneId);
                CalendarPreferences.setLockedTimeZoneId(context, "");
            }
        }
        return zone;
    }
}
