package com.plusonelabs.calendar;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ABBREVIATE_DATES;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ABBREVIATE_DATES_DEFAULT;

public class DateUtil {

    private static final String COMMA_SPACE = ", ";
    private static volatile DateTime mNow = null;
    private static volatile DateTime mNowSetAt = DateTime.now();

    public static boolean isMidnight(DateTime date) {
        return date.isEqual(date.withTimeAtStartOfDay());
    }

    public static String createDateString(Context context, DateTime dateTime) {
        DateTime timeAtStartOfToday = DateTime.now().withTimeAtStartOfDay();
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_ABBREVIATE_DATES, PREF_ABBREVIATE_DATES_DEFAULT)) {
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
        TimeZone timeZoneStored = null;
        long millis = dateTime.toDate().getTime();
        if (CalendarPreferences.isTimeZoneLocked(context)) {
            timeZoneStored = TimeZone.getDefault();
            TimeZone.setDefault(getCurrentTimeZone(context).toTimeZone());
        }
        String formatted = DateUtils.formatDateTime(context, millis, flags);
        if (timeZoneStored != null) {
            TimeZone.setDefault(timeZoneStored);
        }
        return formatted;
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
        String lockedTimeZoneId = CalendarPreferences.getLockedTimeZoneId(context);
        if (!TextUtils.isEmpty(lockedTimeZoneId)) {
            try {
                zone = DateTimeZone.forID(lockedTimeZoneId);
            } catch (IllegalArgumentException e) {
                Log.w("getDefaultTimeZone", "The Locked time zone is not recognized: " + lockedTimeZoneId);
            }
        }
        return zone;
    }
}
