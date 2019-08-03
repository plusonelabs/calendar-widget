package org.andstatus.todoagenda.prefs;

import android.support.annotation.NonNull;

import org.andstatus.todoagenda.provider.EventProviderType;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventSource {
    public final static EventSource EMPTY = new EventSource(EventProviderType.EMPTY, 0, "Empty", "", 0);
    public static final String STORE_SEPARATOR = ",";

    public final EventProviderType providerType;
    private int id;
    private String title;
    private String summary;
    private int color;

    public static List<EventSource> fromStringSet(Collection<String> sources) {
        if (sources == null) return Collections.emptyList();

        List<EventSource> eventSources = new ArrayList<>();
        for(String stringSource: sources) {
            EventSource source = fromStoredString(stringSource);
            if (source != EMPTY) {
                eventSources.add(source);
            }
        }
        return eventSources;
    }

    public static EventSource fromStoredString(String stored) {
        String[] values = stored == null ? new String[]{} : stored.split(STORE_SEPARATOR, 2);
        switch (values.length) {
            case 1:
                return new EventSource(EventProviderType.CALENDAR, parseIntSafe(values[0]), "", "", 0);
            case 2:
                EventProviderType providerType = EventProviderType.fromId(parseIntSafe(values[0]));
                int id = parseIntSafe(values[1]);
                return providerType == EventProviderType.EMPTY || id == 0
                        ? EMPTY
                        : new EventSource(providerType, id, "", "", 0);
            default:
                return EMPTY;
        }
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Set<String> toStringSet(Collection<EventSource> sources) {
        Set<String> stringSet = new HashSet<>();
        for(EventSource source: sources) {
            stringSet.add(source.toStoredString());
        }
        return stringSet;
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
    public static JSONArray toJsonArray(List<EventSource> eventSources) {
        List<String> strings = new ArrayList<>();
        for(EventSource source: eventSources) {
            strings.add(source.toStoredString());
        }
        return new JSONArray(strings);
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
