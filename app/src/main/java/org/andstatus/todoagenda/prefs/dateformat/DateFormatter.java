package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;
import android.text.format.DateUtils;

import org.andstatus.todoagenda.R;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Instant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author yvolk@yurivolkov.com
 */
public class DateFormatter {
    private final Context context;
    private final DateFormatValue dateFormatValue;
    private final DateTime now;
    Locale locale = Locale.getDefault();

    public DateFormatter(Context context, DateFormatValue dateFormatValue, DateTime now) {
        this.context = context;
        this.dateFormatValue = dateFormatValue;
        this.now = now;
    }

    public CharSequence formatMillis(long millis) {
        try {
            if(dateFormatValue.hasPattern()) {
                return formatDateCustom(millis, dateFormatValue.getPattern());
            }

            switch (dateFormatValue.type) {
                case HIDDEN:
                    return "";
                case DEVICE_DEFAULT:
                    return formatDateTime(millis, DateUtils.FORMAT_SHOW_DATE);
                case DEFAULT_WEEKDAY:
                    return formatDateTime(millis, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
                case ABBREVIATED:
                    return formatDateTime(millis, DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_WEEKDAY);
                case DEFAULT_DAYS:
                    return getDaysFromTodayString(context, getDaysFromToday(millis)) + ", " +
                            formatDateTime(millis, DateUtils.FORMAT_SHOW_DATE);
                case NUMBER_OF_DAYS:
                    return getDaysFromTodayString(context, getDaysFromToday(millis));
                default:
                    return "(not implemented)";
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    private String formatDateTime(long millis, int flags) {
        return DateUtils.formatDateRange(context,
                new Formatter(new StringBuilder(50), locale),
                millis,
                millis,
                flags,
                now.getZone().getID())
                .toString();
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
                return Math.abs(daysFromToday) > 9999 ? "..." : Integer.toString(daysFromToday);
        }
    }

    public int getDaysFromToday(long millis) {
        return Days.daysBetween(now.withTimeAtStartOfDay(),
                Instant.ofEpochMilli(millis)).getDays();
    }

    private String formatDateCustom(long millis, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
            Date date = new Date(millis);
            return format.format(date);
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

}