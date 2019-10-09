package org.andstatus.todoagenda.prefs;

import android.util.Log;

import org.andstatus.todoagenda.provider.EventProviderType;

import androidx.annotation.NonNull;

public class EventSource {
    public final static EventSource EMPTY = new EventSource(EventProviderType.EMPTY, 0, "Empty", "", 0);
    public static final String STORE_SEPARATOR = ",";

    public final EventProviderType providerType;
    private int id;
    private String title;
    private String summary;
    private int color;

    static EventSource fromStoredString(String stored) {
        if (stored == null) return  EMPTY;

        String[] values = stored.split(STORE_SEPARATOR, 2);
        switch (values.length) {
            case 1:
                return fromTypeAndId(EventProviderType.CALENDAR, parseIntSafe(values[0]));
            case 2:
                return fromTypeAndId(EventProviderType.fromId(parseIntSafe(values[0])), parseIntSafe(values[1]));
            default:
                return EMPTY;
        }
    }

    public static EventSource fromTypeAndId(EventProviderType providerType, int id) {
        if (providerType == EventProviderType.EMPTY || id == 0) {
            return EMPTY;
        }
        for (OrderedEventSource orderedSource: EventProviderType.getAvailableSources()) {
            EventSource source = orderedSource.source;
            if (source.providerType == providerType && source.id == id) {
                return source;
            }
        }
        Log.w(EventSource.class.getSimpleName(), "Unexpect source " + providerType + ", id:" + id);
        return new EventSource(providerType, id, "", "", 0);
    }


    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return title + " " + summary + ", id:" + id;
    }

    @NonNull
    public String toStoredString() {
        return providerType.id + STORE_SEPARATOR + id;
    }

    public EventSource(EventProviderType providerType, int id, String title, String summary, int color) {
        this.providerType = providerType;
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventSource source = (EventSource) o;

        if (id != source.id) return false;
        return providerType == source.providerType;
    }

    @Override
    public int hashCode() {
        int result = providerType.hashCode();
        result = 31 * result + id;
        return result;
    }
}
