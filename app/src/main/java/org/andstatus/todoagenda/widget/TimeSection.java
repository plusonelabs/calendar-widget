package org.andstatus.todoagenda.widget;

public enum TimeSection {
    PAST("PastTime"),
    TODAY("TodayTime"),
    FUTURE("FutureTime"),
    ALL("AllTime");

    public final String preferenceCategoryKey;

    TimeSection(String preferenceCategoryKey) {
        this.preferenceCategoryKey = preferenceCategoryKey;
    }

    public <T> T select(T past, T today, T future) {
        switch (this) {
            case PAST:
                return past;
            case TODAY:
                return today;
            default:
                return future;
        }
    }
}
