package com.plusonelabs.calendar;

import org.joda.time.DateTime;

public enum PastTime {
    NONE(0),
    ONE_HOUR(1),
    TWO_HOURS(2),
    FOUR_HOURS(4),
    ONE_DAY(24),
    TODAY(0) {
        @Override
        public long getTime(long millisNow) {
            return new DateTime(millisNow).withTimeAtStartOfDay().getMillis();
        }
    };

    private final int hoursAgo;

    PastTime(int hoursAgo) {
        this.hoursAgo = hoursAgo;
    }

    public long getTime(long millisNow) {
        return new DateTime(millisNow).minusHours(hoursAgo).getMillis();
    }

    public static PastTime fromValue(String value) {
        try {
            return PastTime.valueOf(value);
        } catch (Exception e) {
            return NONE;
        }
    }

}
