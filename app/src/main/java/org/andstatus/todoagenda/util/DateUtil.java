package org.andstatus.todoagenda.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.function.Supplier;

import static org.andstatus.todoagenda.util.MyClock.isDateDefined;

public class DateUtil {
    private static final String COMMA_SPACE = ", ";
    private static final String TWELVE = "12";
    private static final String AUTO = "auto";
    public static final String EMPTY_STRING = "";

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
            DateTime timeAtStartOfToday = settings.clock().now().withTimeAtStartOfDay();
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
        return DateUtils.formatDateRange(settings.getContext(),
                new Formatter(new StringBuilder(50), Locale.getDefault()),
                dateTime.getMillis(),
                dateTime.getMillis(),
                flags,
                settings.clock().getZone().getID())
            .toString();
    }

    public static String formatTime(Supplier<InstanceSettings> settingsSupplier, @Nullable DateTime time) {
        if (!isDateDefined(time)) return EMPTY_STRING;

        InstanceSettings settings = settingsSupplier.get();
        String dateFormat = settings.getDateFormat();
        if (!DateFormat.is24HourFormat(settings.getContext()) && dateFormat.equals(AUTO)
                || dateFormat.equals(TWELVE)) {
            return formatDateTime(settings, time,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_12HOUR);
        }
        return formatDateTime(settings, time,
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
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

    public static String formatLogDateTime(long time) {
        for (int ind = 0; ind < 2; ind++) {
            // see http://stackoverflow.com/questions/16763968/android-text-format-dateformat-hh-is-not-recognized-like-with-java-text-simple
            String formatString = ind==0 ? "yyyy-MM-dd-HH-mm-ss-SSS" : "yyyy-MM-dd-kk-mm-ss-SSS";
            SimpleDateFormat format = new SimpleDateFormat(formatString);
            StringBuffer buffer = new StringBuffer();
            format.format(new Date(time), buffer, new FieldPosition(0));
            String strTime = buffer.toString();
            if (!strTime.contains("HH")) {
                return strTime;
            }
        }
        return Long.toString(time); // Fallback if above doesn't work
    }

    public static boolean isSameDate(@Nullable DateTime date, @Nullable DateTime other) {
        if (date == null && other == null) return true;
        if (date == null || other == null) return false;

        return date.equals(other);
    }

    public static boolean isSameDay(@Nullable DateTime date, @Nullable DateTime other) {
        if (date == null && other == null) return true;
        if (date == null || other == null) return false;

        return date.year().equals(other.year()) && date.dayOfYear().equals(other.dayOfYear());
    }
}
