package org.andstatus.todoagenda.prefs;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yvolk@yurivolkov.com
 */
public class OrderedEventSource {
    private static final String TAG = OrderedEventSource.class.getSimpleName();
    public final static OrderedEventSource EMPTY = new OrderedEventSource(EventSource.EMPTY, 0);

    public final EventSource source;
    public final int order;

    public OrderedEventSource(EventSource source, int order) {
        this.source = source;
        this.order = order;
    }

    public static List<OrderedEventSource> fromJsonString(String sources) {
        if (sources == null) return Collections.emptyList();

        try {
            return fromJsonArray(new JSONArray(sources));
        } catch (JSONException e) {
            Log.w(TAG, "Failed to parse event sources: " + sources, e);
            return Collections.emptyList();
        }
    }

    @NonNull
    public static List<OrderedEventSource> fromJsonArray(JSONArray jsonArray) {
        List<OrderedEventSource> list = new ArrayList<>();
        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject jsonObject = jsonArray.optJSONObject(index);
            EventSource source = jsonObject == null
                    ? EventSource.fromStoredString(jsonArray.optString(index))
                    : EventSource.fromJson(jsonObject).toAvailable();
            if (source != EventSource.EMPTY) {
                add(list, source);
            }
        }
        return list;
    }

    public static List<OrderedEventSource> fromSources(List<EventSource> sources) {
        return addAll(new ArrayList<OrderedEventSource>(), sources);
    }

    static List<OrderedEventSource> addAll(List<OrderedEventSource> list, List<EventSource> sources) {
        for(EventSource source: sources) {
            add(list, source);
        }
        return list;
    }

    private static void add(List<OrderedEventSource> list, EventSource source) {
        if (source != EventSource.EMPTY) {
            list.add(new OrderedEventSource(source, list.size() + 1));
        }
    }

    @NonNull
    public static String toJsonString(List<OrderedEventSource> eventSources) {
        return toJsonArray(eventSources).toString();
    }

    @NonNull
    public static JSONArray toJsonArray(List<OrderedEventSource> sources) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        for(OrderedEventSource source: sources) {
            jsonObjects.add(source.source.toJson());
        }
        return new JSONArray(jsonObjects);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderedEventSource that = (OrderedEventSource) o;

        return source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public String toString() {
        return "order:" + order + ", " + source;
    }
}
