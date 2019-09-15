package org.andstatus.todoagenda.widget;

public enum TimeSection {
    PAST,
    TODAY,
    FUTURE;

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
