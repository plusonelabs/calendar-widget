package org.andstatus.todoagenda.util;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * A clock, the can be changed independently from a Device clock
 * @author yvolk@yurivolkov.com
 */
public class MyClock {
    public static final DateTime DATETIME_MIN = new DateTime(0, DateTimeZone.UTC);
    public static final DateTime DATETIME_MAX = new DateTime(Long.MAX_VALUE, DateTimeZone.UTC);

    private final DateTimeZone zone;
    private volatile DateTime mNow = null;
    private volatile DateTime mNowSetAt = DateTime.now();

    public MyClock(DateTimeZone zone) {
        this.zone = zone;
    }

    public void setNow(DateTime now) {
        mNowSetAt = DateTime.now();
        mNow = now;
    }

    /**
     * Usually returns real "now", but may be #setNow to some other time for testing purposes
     */
    public DateTime now(DateTimeZone zone) {
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

    public DateTimeZone getZone(DateTimeZone defaultValue) {
        DateTime now = mNow;
        return now == null ? defaultValue : now.getZone();
    }

    public boolean isToday(@Nullable DateTime date) {
        return isDateDefined(date) && !isBeforeToday(date) && date.isBefore(now(date.getZone()).plusDays(1).withTimeAtStartOfDay());
    }

    public boolean isBeforeToday(@Nullable DateTime date) {
        return isDateDefined(date) && date.isBefore(now(date.getZone()).withTimeAtStartOfDay());
    }

    public boolean isAfterToday(@Nullable DateTime date) {
        return isDateDefined(date) && !date.isBefore(now(date.getZone()).withTimeAtStartOfDay().plusDays(1));
    }

    public boolean isBeforeNow(@Nullable DateTime date) {
        return isDateDefined(date) && date.isBefore(now(date.getZone()));
    }

    public DateTime startOfTomorrow(DateTimeZone zone) {
        return startOfNextDay(now(zone));
    }

    public static DateTime startOfNextDay(DateTime date) {
        return date.plusDays(1).withTimeAtStartOfDay();
    }

    public static boolean isDateDefined(@Nullable DateTime dateTime) {
        return dateTime != null && dateTime.isAfter(DATETIME_MIN) && dateTime.isBefore(DATETIME_MAX);
    }
}
