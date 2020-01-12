package org.andstatus.todoagenda.util;

import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.SnapshotMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * A clock, the can be changed independently from a Device clock
 * @author yvolk@yurivolkov.com
 */
public class MyClock {
    public static final DateTime DATETIME_MIN = new DateTime(0, DateTimeZone.UTC);
    public static final DateTime DATETIME_MAX = new DateTime(Long.MAX_VALUE, DateTimeZone.UTC);

    private volatile SnapshotMode snapshotMode = SnapshotMode.defaultValue;
    private volatile DateTime snapshotDate = null;
    private volatile DateTime snapshotDateSetAt = null;
    private volatile String lockedTimeZoneId = "";
    private volatile DateTimeZone zone;

    public MyClock() {
        zone = DateTimeZone.getDefault();
    }

    public void setLockedTimeZoneId(String lockedTimeZoneId) {
        this.lockedTimeZoneId = DateUtil.validatedTimeZoneId(lockedTimeZoneId);
        zone = getLockedOrDefaultZone();
    }

    private DateTimeZone getLockedOrDefaultZone() {
        return isTimeZoneLocked()
            ? DateTimeZone.forID(DateUtil.validatedTimeZoneId(lockedTimeZoneId))
            : DateTimeZone.getDefault();
    }

    public String getLockedTimeZoneId() {
        return snapshotMode == SnapshotMode.SNAPSHOT_TIME
            ? zone.getID()
            : lockedTimeZoneId;
    }

    public boolean isTimeZoneLocked() {
        return snapshotMode == SnapshotMode.SNAPSHOT_TIME || !StringUtil.isEmpty(lockedTimeZoneId);
    }

    public void setSnapshotMode(SnapshotMode snapshotMode) {
        this.snapshotMode = snapshotMode;
    }

    public void setSnapshotDate(DateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
        snapshotDateSetAt = DateTime.now();
        zone = snapshotDate == null
                ? getLockedOrDefaultZone()
                : snapshotDate.getZone();
    }

    public boolean isSnapshotDateSet() {
        return snapshotDate != null;
    }

    public SnapshotMode getSnapshotMode() {
        return snapshotDate == null ? SnapshotMode.LIVE_DATA : snapshotMode;
    }

    public void setFromPrevious(MyClock prevClock) {
        if (prevClock.isTimeZoneLocked()) {
            setLockedTimeZoneId(prevClock.lockedTimeZoneId);
        }
        if (prevClock.snapshotDate != null) {
            setSnapshotDate(prevClock.snapshotDate);
        }
    }

    /**
     * Usually returns real "now", but may be #setNow to some other time for testing purposes
     */
    public DateTime now() {
        return now(zone);
    }

    public DateTime now(DateTimeZone zone) {
        DateTime nowSetAt = null;
        DateTime now = null;
        if (getSnapshotMode() == SnapshotMode.SNAPSHOT_TIME) {
            do {
                nowSetAt = snapshotDateSetAt;
                now = snapshotDate;
            } while (nowSetAt != snapshotDateSetAt); // Ensure concurrent consistency
        }

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
