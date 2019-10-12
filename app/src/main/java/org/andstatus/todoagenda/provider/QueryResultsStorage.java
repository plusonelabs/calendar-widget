package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.RemoteViewsFactory;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.andstatus.todoagenda.util.DateUtil.formatLogDateTime;

/**
 * @author yvolk@yurivolkov.com
 */
public class QueryResultsStorage {

    private static final String TAG = QueryResultsStorage.class.getSimpleName();
    private static final String KEY_RESULTS_VERSION = "resultsVersion";
    private static final int RESULTS_VERSION = 3;
    private static final String KEY_RESULTS = "results";
    public static final String KEY_SETTINGS = "settings";

    private static volatile QueryResultsStorage theStorage = null;

    private final List<QueryResult> results = new CopyOnWriteArrayList<>();

    public static boolean store(QueryResult result) {
        QueryResultsStorage storage = theStorage;
        if (storage != null) {
            storage.results.add(result);
            return (storage == theStorage);
        }
        return false;
    }

    public static void shareEventsForDebugging(Context context, int widgetId) {
        final String method = "shareEventsForDebugging";
        try {
            Log.i(TAG, method + " started");
            setNeedToStoreResults(true);
            RemoteViewsFactory factory = new RemoteViewsFactory(context, widgetId);
            factory.onDataSetChanged();
            String results = theStorage.toJsonString(context, widgetId);
            if (TextUtils.isEmpty(results)) {
                Log.i(TAG, method + "; Nothing to share");
            } else {
                InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
                String fileName = (settings.getWidgetInstanceName() + "-" + context.getText(R.string.app_name))
                        .replaceAll("\\W+", "-") +
                        "-shareEvents-" + formatLogDateTime(System.currentTimeMillis()) +
                        ".json";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                intent.putExtra(Intent.EXTRA_TEXT, results);
                context.startActivity(
                        Intent.createChooser(intent, context.getText(R.string.share_events_for_debugging_title)));
                Log.i(TAG, method + "; Shared " + results);
            }
        } finally {
            setNeedToStoreResults(false);
        }
    }

    public static boolean getNeedToStoreResults() {
        return theStorage != null;
    }

    public static void setNeedToStoreResults(boolean needToStoreResults) {
        if (needToStoreResults) {
            theStorage = new QueryResultsStorage();
        } else {
            theStorage = null;
        }
    }

    public static QueryResultsStorage getStorage() {
        return theStorage;
    }

    public List<QueryResult> getResults() {
        return results;
    }

    private String toJsonString(Context context, int widgetId) {
        try {
            return toJson(context, widgetId).toString(2);
        } catch (JSONException e) {
            return "Error while formatting data " + e;
        }
    }

    JSONObject toJson(Context context, int widgetId) throws JSONException {
        JSONArray resultsArray = new JSONArray();
        for (QueryResult result : results) {
            if (result.getWidgetId() == widgetId) {
                resultsArray.put(result.toJson());
            }
        }
        JSONObject json = WidgetData.fromWidgetId(context, widgetId).toJson();
        json.put(KEY_RESULTS_VERSION, RESULTS_VERSION);
        json.put(KEY_RESULTS, resultsArray);
        return json;
    }

    static QueryResultsStorage fromTestData(Context context, JSONObject json) throws JSONException {
        WidgetData widgetData = WidgetData.fromJson(json);
        InstanceSettings settings = widgetData.getSettings(context);

        // TODO: Map Calendars when moving between devices

        AllSettings.getInstances(context).put(settings.getWidgetId(), settings);
        QueryResultsStorage results = new QueryResultsStorage();
        JSONArray jsonResults = json.getJSONArray(KEY_RESULTS);
        for (int ind = 0; ind < jsonResults.length(); ind++) {
            results.results.add(QueryResult.fromJson(jsonResults.getJSONObject(ind), settings.getWidgetId()));
        }
        if (!results.results.isEmpty()) {
            DateTime now = results.results.get(0).getExecutedAt().toDateTime(DateTimeZone.getDefault());
            DateUtil.setNow(now);
        }
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryResultsStorage results = (QueryResultsStorage) o;

        if (this.results.size() != results.results.size()) {
            return false;
        }
        for (int ind = 0; ind < this.results.size(); ind++) {
            if (!this.results.get(ind).equals(results.results.get(ind))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int ind = 0; ind < results.size(); ind++) {
            result = 31 * result + results.get(ind).hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return TAG + ":" + results;
    }
}
