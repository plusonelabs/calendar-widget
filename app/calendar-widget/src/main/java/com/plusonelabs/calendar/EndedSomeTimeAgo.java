package com.plusonelabs.calendar;

import org.joda.time.DateTime;

public enum EndedSomeTimeAgo {
    NONE("NONE", 0),
    TODAY("TODAY", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay();
        }
    },
    ONE_HOUR("ONE_HOUR", 1),
    TWO_HOURS("TWO_HOURS", 2),
    FOUR_HOURS("FOUR_HOURS", 4),
    YESTERDAY("YESTERDAY", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusDays(1);
        }
    };

    private final String value;
    private final int hoursAgo;

    EndedSomeTimeAgo(String valueIn, int hoursAgo) {
        this.value = valueIn;
        this.hoursAgo = hoursAgo;
    }

    public DateTime endedAt(DateTime now) {
        return now.minusHours(hoursAgo);
    }

    public static EndedSomeTimeAgo fromValue(String valueIn) {
        EndedSomeTimeAgo ended = NONE;
        for (EndedSomeTimeAgo item : EndedSomeTimeAgo.values()) {
            if (item.value.equals(valueIn)) {
                ended = item;
                break;
            }
        }
        return ended;
    }

    public String save() {
        return value;
    }
}
