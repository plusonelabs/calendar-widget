package org.andstatus.todoagenda;

import org.joda.time.DateTime;

public enum EndedSomeTimeAgo {
    NONE("NONE", 0),
    ONE_HOUR("ONE_HOUR", 1),
    TWO_HOURS("TWO_HOURS", 2),
    FOUR_HOURS("FOUR_HOURS", 4),
    TODAY("TODAY", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay();
        }
    },
    YESTERDAY("YESTERDAY", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusDays(1);
        }
    },
    ONE_WEEK("ONE_WEEK", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusDays(7);
        }
    },
    TWO_WEEKS("TWO_WEEKS", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusDays(14);
        }
    },
    ONE_MONTH("ONE_MONTH", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusMonths(1);
        }
    },
    TWO_MONTHS("TWO_MONTHS", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusMonths(2);
        }
    },
    THREE_MONTHS("THREE_MONTHS", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusMonths(3);
        }
    },
    SIX_MONTHS("SIX_MONTHS", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusMonths(6);
        }
    },
    ONE_YEAR("ONE_YEAR", 0) {
        @Override
        public DateTime endedAt(DateTime now) {
            return now.withTimeAtStartOfDay().minusYears(1);
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
