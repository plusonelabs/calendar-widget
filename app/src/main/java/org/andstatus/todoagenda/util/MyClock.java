package org.andstatus.todoagenda.util;

import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.SnapshotMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Minutes;

/**
 * A clock, the can be changed independently from a Device clock
 * @author yvolk@yurivolkov.com
 */
public class MyClock {
    public static final DateTime DATETIME_MIN = new DateTime(0, DateTimeZone.UTC).withTimeAtStartOfDay();
    public static final DateTime DATETIME_MAX = new DateTime(5000, 1, 1, 0, 0, DateTimeZone.UTC).withTimeAtStartOfDay();

    private volatile SnapshotMode snapshotMode = SnapshotMode.defaultValue;
    private volatile DateTime snapshotDate = null;
    private volatile DateTime snapshotDateSetAt = null;
    private volatile String lockedTimeZoneId = "";
    private volatile DateTimeZone zone;

    public MyClock() {
        zone = DateTimeZone.getDefault();
    }

    public void setSnapshotMode(SnapshotMode snapshotMode) {
        this.snapshotMode = snapshotMode;
        updateZone();
    }

    public void setSnapshotDate(DateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
        snapshotDateSetAt = DateTime.now();
        updateZone();
    }

    public void setLockedTimeZoneId(String timeZoneId) {
        lockedTimeZoneId = DateUtil.validatedTimeZoneId(timeZoneId);
        updateZone();
    }

    private void updateZone() {
        if (snapshotMode == SnapshotMode.SNAPSHOT_TIME && snapshotDate != null) {
            zone = snapshotDate.getZone();
        } else if (StringUtil.nonEmpty(lockedTimeZoneId)) {
            zone = DateTimeZone.forID(lockedTimeZoneId);
        } else {
            zone = DateTimeZone.getDefault();
        }
    }

    public String getLockedTimeZoneId() {
        return lockedTimeZoneId;
    }

    public SnapshotMode getSnapshotMode() {
        return snapshotMode;
    }

    /**
     * Usually returns real "now", but may be #setNow to some other time for testing purposes
     */
    public DateTime now() {
        return now(zone);
    }

    public DateTime now(DateTimeZone zone) {
        DateTime snapshotDate = this.snapshotDate;
        if (getSnapshotMode() == SnapshotMode.SNAPSHOT_TIME && snapshotDate != null) {
            return PermissionsUtil.isTestMode()
                    ? getTimeMachineDate(zone)
                    : snapshotDate.withZone(zone);
        } else {
            return DateTime.now(zone);
        }
    }

    private DateTime getTimeMachineDate(DateTimeZone zone) {
        DateTime nowSetAt = null;
        DateTime now = null;
        do {
            nowSetAt = snapshotDateSetAt;
            now = snapshotDate;
        } while (nowSetAt != snapshotDateSetAt); // Ensure concurrent consistency

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

    public DateTimeZone getZone() {
        return zone;
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

    public int getNumberOfDaysTo(DateTime date) {
        return Days.daysBetween(
                now(date.getZone()).withTimeAtStartOfDay(),
                date.withTimeAtStartOfDay())
            .getDays();
    }

    public int getNumberOfMinutesTo(DateTime date) {
        return Minutes.minutesBetween(now(date.getZone()), date)
                .getMinutes();
    }

    public DateTime startOfTomorrow() {
        return startOfNextDay(now(zone));
    }

    public static DateTime startOfNextDay(DateTime date) {
        return date.plusDays(1).withTimeAtStartOfDay();
    }

    public static boolean isDateDefined(@Nullable DateTime dateTime) {
        return dateTime != null && dateTime.isAfter(DATETIME_MIN) && dateTime.isBefore(DATETIME_MAX);
    }
}
