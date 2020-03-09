package org.andstatus.todoagenda.util;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private static final String TWELVE = "12";
    private static final String AUTO = "auto";
    public static final String EMPTY_STRING = "";

    public static boolean isMidnight(DateTime date) {
        return date.isEqual(date.withTimeAtStartOfDay());
    }

    public static String formatTime(Supplier<InstanceSettings> settingsSupplier, @Nullable DateTime time) {
        if (!isDateDefined(time)) return EMPTY_STRING;

        InstanceSettings settings = settingsSupplier.get();
        String timeFormat = settings.getTimeFormat();
        if (!DateFormat.is24HourFormat(settings.getContext()) && timeFormat.equals(AUTO)
                || timeFormat.equals(TWELVE)) {
            return formatDateTime(settings, time,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_12HOUR);
        }
        return formatDateTime(settings, time,
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
    }

    private static String formatDateTime(InstanceSettings settings, DateTime dateTime, int flags) {
        return DateUtils.formatDateRange(settings.getContext(),
                new Formatter(new StringBuilder(50), Locale.getDefault()),
                dateTime.getMillis(),
                dateTime.getMillis(),
                flags,
                settings.clock().getZone().getID())
                .toString();
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

    public static DateTime exactMinutesPlusMinutes(DateTime nowIn, int periodMinutes) {
        DateTime now = nowIn.plusMinutes(1);
        return new DateTime(now.getYear(), now.getMonthOfYear(),
                now.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour(), now.getZone())
                .plusMinutes(periodMinutes);
    }
}
