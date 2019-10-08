package org.andstatus.todoagenda.prefs;

import android.util.Log;

import org.andstatus.todoagenda.provider.EventProviderType;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public class EventSource {
    public final static EventSource EMPTY = new EventSource(EventProviderType.EMPTY, 0, "Empty", "", 0);
    public static final String STORE_SEPARATOR = ",";

    public final EventProviderType providerType;
    private int id;
    private String title;
    private String summary;
    private int color;

    public static List<EventSource> fromJsonString(String sources) {
        if (sources == null) return Collections.emptyList();

        try {
            return fromJsonArray(new JSONArray(sources));
        } catch (JSONException e) {
            Log.w(EventSource.class.getSimpleName(), "Failed to parse event sources: " + sources, e);
            return Collections.emptyList();
        }
    }

    static EventSource fromStoredString(String stored) {
        String[] values = stored == null ? new String[]{} : stored.split(STORE_SEPARATOR, 2);
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
        for (EventSource source: EventProviderType.getAvailableSources()) {
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
    public static List<EventSource> fromJsonArray(JSONArray jsonArray) {
        List<EventSource> list = new ArrayList<>();
        for (int index = 0; index < jsonArray.length(); index++) {
            String value = jsonArray.optString(index);
            if (value != null) {
                EventSource source = fromStoredString(value);
                if (source != EMPTY) {
                    list.add(source);
                }
            }
        }
        return list;
    }

    @NonNull
    public static String toJsonString(List<EventSource> eventSources) {
        return toJsonArray(eventSources).toString();
    }

    @NonNull
    public static JSONArray toJsonArray(List<EventSource> eventSources) {
        List<String> strings = new ArrayList<>();
        for(EventSource source: eventSources) {
            strings.add(source.toStoredString());
        }
        return new JSONArray(strings);
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
