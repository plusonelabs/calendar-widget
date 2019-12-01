package org.andstatus.todoagenda.prefs;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.provider.EventProviderType;
import org.json.JSONException;
import org.json.JSONObject;

public class EventSource {
    private static final String TAG = EventSource.class.getSimpleName();
    public final static EventSource EMPTY = new EventSource(EventProviderType.EMPTY, 0, "Empty", "", 0, false);
    public static final String STORE_SEPARATOR = ",";

    private static final String KEY_PROVIDER_TYPE = "providerType";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SUMMARY = "summary";
    private static final String KEY_COLOR = "color";
    private static final String KEY_IS_AVAILABLE = "isAvailable";

    public final EventProviderType providerType;
    private int id;
    private String title;
    private String summary;
    private int color;
    public final boolean isAvailable;

    static EventSource fromStoredString(String stored) {
        if (stored == null) return  EMPTY;

        String[] values = stored.split(STORE_SEPARATOR, 2);
        switch (values.length) {
            case 1:
                return fromTypeAndId(EventProviderType.CALENDAR, ApplicationPreferences.parseIntSafe(values[0]));
            case 2:
                return fromTypeAndId(EventProviderType.fromId(ApplicationPreferences.parseIntSafe(values[0])),
                        ApplicationPreferences.parseIntSafe(values[1]));
            default:
                return EMPTY;
        }
    }

    private static EventSource fromTypeAndId(EventProviderType providerType, int id) {
        if (providerType == EventProviderType.EMPTY || id == 0) {
            return EMPTY;
        }
        for (OrderedEventSource orderedSource: EventProviderType.getAvailableSources()) {
            EventSource source = orderedSource.source;
            if (source.providerType == providerType && source.id == id) {
                return source;
            }
        }
        Log.w(TAG, "Unavailable source " + providerType + ", id:" + id);
        return new EventSource(providerType, id, "(id:" + id + ")", "", Color.RED, false);
    }

    public EventSource toAvailable() {
        if (this == EMPTY || isAvailable) return this;

        for (OrderedEventSource orderedSource: EventProviderType.getAvailableSources()) {
            EventSource source = orderedSource.source;
            if (source.providerType == providerType && source.id == id &&
                    source.title.equals(title) && source.summary.equals(summary)) {
                return source;
            }
        }
        for (OrderedEventSource orderedSource: EventProviderType.getAvailableSources()) {
            EventSource source = orderedSource.source;
            if (source.providerType == providerType && source.id == id && source.title.equals(title)) {
                return source;
            }
        }
        for (OrderedEventSource orderedSource: EventProviderType.getAvailableSources()) {
            EventSource source = orderedSource.source;
            if (source.providerType == providerType &&
                    source.title.equals(title) && source.summary.equals(summary)) {
                return source;
            }
        }
        for (OrderedEventSource orderedSource: EventProviderType.getAvailableSources()) {
            EventSource source = orderedSource.source;
            if (source.providerType == providerType && source.title.equals(title)) {
                return source;
            }
        }
        for (OrderedEventSource orderedSource: EventProviderType.getAvailableSources()) {
            EventSource source = orderedSource.source;
            if (source.providerType == providerType && source.id == id) {
                return source;
            }
        }
        Log.i(TAG, "Unavailable source " + this);
        return this;
    }

    static EventSource fromJson(JSONObject json) {
        if (json == null || !json.has(KEY_PROVIDER_TYPE)) return EMPTY;

        EventProviderType providerType = EventProviderType.fromId(json.optInt(KEY_PROVIDER_TYPE));
        int id = json.optInt(KEY_ID);
        String title = json.optString(KEY_TITLE);
        String summary = json.optString(KEY_SUMMARY);
        int color = json.optInt(KEY_COLOR);
        if (providerType == EventProviderType.EMPTY || id == 0) {
            return EMPTY;
        }
        return new EventSource(providerType, id, title, summary, color, false);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_PROVIDER_TYPE, providerType.id);
            json.put(KEY_ID, id);
            json.put(KEY_TITLE, title);
            json.put(KEY_SUMMARY, summary);
            json.put(KEY_COLOR, color);
            json.put(KEY_IS_AVAILABLE, isAvailable);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @NonNull
    @Override
    public String toString() {
        return this == EMPTY
                ? "(Empty)"
                : providerType.name() + " " + title + ", " + summary + ", id:" + id +
                (isAvailable ? "" : ", unavailable");
    }

    @NonNull
    public String toStoredString() {
        return providerType.id + STORE_SEPARATOR + id;
    }

    public EventSource(EventProviderType providerType, int id, String title, String summary, int color, boolean isAvailable) {
        this.providerType = providerType;
        this.id = id;
        this.title = title == null ? "" : title;
        this.summary = summary == null ? "" : summary;
        this.color = color;
        this.isAvailable = isAvailable;
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
