package org.andstatus.todoagenda.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Formatter;
import java.util.Locale;

import androidx.annotation.NonNull;

public class DateUtil {

    private static final String COMMA_SPACE = ", ";
    private static volatile DateTime mNow = null;
    private static volatile DateTime mNowSetAt = DateTime.now();
    public static final DateTime DATETIME_MIN = new DateTime(0, DateTimeZone.UTC);

    public static boolean isMidnight(DateTime date) {
        return date.isEqual(date.withTimeAtStartOfDay());
    }

    public static String createDayHeaderTitle(InstanceSettings settings, DateTime dateTime) {
        return createDateString(settings, dateTime, true);
    }

    public static String createDateString(InstanceSettings settings, DateTime dateTime) {
        return createDateString(settings, dateTime, false);
    }

    private static String createDateString(InstanceSettings settings, DateTime dateTime, boolean forDayHeader) {
        if (settings.getAbbreviateDates()) {
            return formatDateTime(settings, dateTime,
                    DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_WEEKDAY);
        }
        if (forDayHeader) {
            DateTime timeAtStartOfToday = DateTime.now().withTimeAtStartOfDay();
            if (dateTime.withTimeAtStartOfDay().isEqual(timeAtStartOfToday)) {
                return createDateString(settings, dateTime, settings.getContext().getString(R.string.today));
            } else if (dateTime.withTimeAtStartOfDay().isEqual(timeAtStartOfToday.plusDays(1))) {
                return createDateString(settings, dateTime, settings.getContext().getString(R.string.tomorrow));
            }
        }
        return formatDateTime(settings, dateTime,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
    }

    private static String createDateString(InstanceSettings settings, DateTime dateTime, String prefix) {
        return prefix + COMMA_SPACE + formatDateTime(settings, dateTime, DateUtils.FORMAT_SHOW_DATE);
    }

    public static String formatDateTime(InstanceSettings settings, DateTime dateTime, int flags) {
        return settings.isTimeZoneLocked() ?
                formatDateTimeAtTimeZone(settings, dateTime, flags, settings.getLockedTimeZoneId()) :
                DateUtils.formatDateTime(settings.getContext(), dateTime.getMillis(), flags);
    }

    private static String formatDateTimeAtTimeZone(InstanceSettings settings, DateTime dateTime,
                                                   int flags, String timeZoneId) {
        return DateUtils.formatDateRange(settings.getContext(),
                new Formatter(new StringBuilder(50), Locale.getDefault()),
                dateTime.getMillis(), dateTime.getMillis(), flags,
                timeZoneId).toString();
    }

    public static CharSequence getDaysFromTodayString(Context context, int daysFromToday) {
        switch (daysFromToday) {
            case -1:
                return context.getText(R.string.yesterday);
            case 0:
                return context.getText(R.string.today);
            case 1:
                return context.getText(R.string.tomorrow);
            default:
                return Integer.toString(daysFromToday);
        }
    }

    public static boolean isToday(DateTime date) {
        return !isBeforeToday(date) && date.isBefore(DateUtil.now(date.getZone()).plusDays(1).withTimeAtStartOfDay());
    }

    public static boolean isBeforeToday(DateTime date) {
        return date.isBefore(DateUtil.now(date.getZone()).withTimeAtStartOfDay());
    }

    public static boolean isAfterToday(DateTime date) {
        return !date.isBefore(DateUtil.now(date.getZone()).withTimeAtStartOfDay().plusDays(1));
    }

    public static boolean isBeforeNow(DateTime date) {
        return date.isBefore(now(date.getZone()));
    }

    public static DateTime startOfTomorrow(DateTimeZone zone) {
        return startOfNextDay(DateUtil.now(zone));
    }
    public static DateTime startOfNextDay(DateTime date) {
        return date.plusDays(1).withTimeAtStartOfDay();
    }

    public static DateTime endOfToday(DateTimeZone zone) {
        return endOfSameDay(DateUtil.now(zone));
    }

    public static DateTime endOfSameDay(DateTime date) {
        return date.plusDays(1).withTimeAtStartOfDay().minusSeconds(1);
    }

    public static void setNow(DateTime now) {
        mNowSetAt = DateTime.now();
        mNow = now;
    }

    /**
     * Usually returns real "now", but may be #setNow to some other time for testing purposes
     */
    public static DateTime now(DateTimeZone zone) {
        DateTime nowSetAt;
        DateTime now;
        do {
            nowSetAt = mNowSetAt;
            now = mNow;
        } while (nowSetAt != mNowSetAt); // Ensure concurrent consistency
        if (now == null) {
            return DateTime.now(zone);
        } else {
            long diffL = DateTime.now().getMillis() - nowSetAt.getMillis();
            int diff = 0;
            if (diffL > 0 && diffL < Integer.MAX_VALUE) {
                diff = (int) diffL;
            }
            return new DateTime(now, zone).plusMillis(diff);
        }
    }

    /**
     * Returns an empty string in a case supplied ID is not a valid Time Zone ID
     */
    @NonNull
    public static String validatedTimeZoneId(String timeZoneId) {
        if (!TextUtils.isEmpty(timeZoneId)) {
            try {
                return DateTimeZone.forID(timeZoneId).getID();
            } catch (IllegalArgumentException e) {
                Log.w("validatedTimeZoneId", "The time zone is not recognized: '" + timeZoneId + "'");
            }
        }
        return "";
    }
}
