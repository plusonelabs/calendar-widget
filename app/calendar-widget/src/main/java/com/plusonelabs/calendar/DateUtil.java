package com.plusonelabs.calendar;

import android.content.Context;
import android.text.format.DateUtils;

import org.joda.time.DateTime;

import java.util.Date;

public class DateUtil {

    private static final String COMMA_SPACE = ", ";
    private static volatile DateTime mNow = null;
    private static volatile DateTime mNowSetAt = DateTime.now();

    public static boolean isMidnight(DateTime date) {
        return date.isEqual(date.withTimeAtStartOfDay());
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
