package com.plusonelabs.calendar;

import org.joda.time.DateTime;

public enum EndedSometimeAgo {
    NONE("NONE", 0),
    TODAY("TODAY", 0) {
        @Override
        public long endedAt(long millisNow) {
            return new DateTime(millisNow).withTimeAtStartOfDay().getMillis();
        }
    },
    ONE_HOUR("ONE_HOUR", 1),
    TWO_HOURS("TWO_HOURS", 2),
    FOUR_HOURS("FOUR_HOURS", 4),
    YESTERDAY("YESTERDAY", 0) {
        @Override
        public long endedAt(long millisNow) {
            return new DateTime(millisNow).withTimeAtStartOfDay().minusDays(1).getMillis();
        }
    };

    private final String value;
    private final int hoursAgo;

    private EndedSometimeAgo(String valueIn, int hoursAgo) {
        this.value = valueIn;
        this.hoursAgo = hoursAgo;
    }

    public long endedAt(long millisNow) {
        return new DateTime(millisNow).minusHours(hoursAgo).getMillis();
    }

    public static EndedSometimeAgo fromValue(String valueIn) {
        EndedSometimeAgo ended = NONE;
        for (EndedSometimeAgo item : EndedSometimeAgo.values()) {
            if (item.value.equals(valueIn)) {
                ended = item;
                break;
            }
        }
        return ended;
    }
}
