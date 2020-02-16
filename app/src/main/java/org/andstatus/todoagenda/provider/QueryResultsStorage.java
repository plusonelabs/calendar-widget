package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.RemoteViewsFactory;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    private static volatile int widgetIdResultsToStore = 0;

    private final List<QueryResult> results = new CopyOnWriteArrayList<>();
    private AtomicReference<DateTime> executedAt = new AtomicReference<>(null);

    public static boolean store(QueryResult result) {
        QueryResultsStorage storage = theStorage;
        if (storage != null) {
            storage.addResult(result);
            return (storage == theStorage);
        }
        return false;
    }

    public static void shareEventsForDebugging(Context context, int widgetId) {
        final String method = "shareEventsForDebugging";
        Log.i(TAG, method + " started");
        InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
        QueryResultsStorage storage = settings.isLiveMode() || !settings.hasResults()
                ? getNewResults(context, widgetId)
                : settings.getResultsStorage();
        String results = storage.toJsonString(context, widgetId);
        if (TextUtils.isEmpty(results)) {
            Log.i(TAG, method + "; Nothing to share");
        } else {
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
    }

    public static QueryResultsStorage getNewResults(Context context, int widgetId) {
        QueryResultsStorage resultsStorage;
        try {
            setNeedToStoreResults(true, widgetId);
            RemoteViewsFactory factory = RemoteViewsFactory.factories.computeIfAbsent(widgetId,
                    id -> new RemoteViewsFactory(context, id, false));
            factory.onDataSetChanged();
            resultsStorage = QueryResultsStorage.theStorage;
        } finally {
            setNeedToStoreResults(false, widgetId);
        }
        return resultsStorage;
    }

    public static boolean getNeedToStoreResults(int widgetId) {
        return theStorage != null && (widgetId == 0 || widgetId == widgetIdResultsToStore);
    }

    public static void setNeedToStoreResults(boolean needToStoreResults, int widgetId) {
        widgetIdResultsToStore = widgetId;
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

    public void addResults(QueryResultsStorage newResults) {
        for (QueryResult result : newResults.getResults()) {
            addResult(result);
        }
        setExecutedAt(newResults.getExecutedAt());
    }

    public void addResult(QueryResult result) {
        executedAt.compareAndSet(null, result.getExecutedAt());
        results.add(result);
    }

    public List<QueryResult> getResults(EventProviderType type, int widgetId) {
        return results.stream().filter(result -> type == EventProviderType.EMPTY || result.providerType == type)
                .filter(result -> widgetId == 0 || result.getWidgetId() == widgetId)
                .collect(Collectors.toList());
    }

    public List<EventProviderType> getProviderTypes(int widgetId) {
        return results.stream()
                .filter(result -> widgetId == 0 || result.getWidgetId() == widgetId)
                .map(result -> result.providerType)
                .distinct()
                .collect(Collectors.toList());
    }

    public Optional<QueryResult> findLast(EventProviderType type) {
        for (int index = results.size() - 1; index >=0; index--) {
            QueryResult result = results.get(index);
            if (type != EventProviderType.EMPTY && result.providerType != type) continue;

            return Optional.of(result);
        }
        return Optional.empty();
    }

    public Optional<QueryResult> getResult(EventProviderType type, int index) {
        int foundIndex = -1;
        for (QueryResult result: results) {
            if (type != EventProviderType.EMPTY && result.providerType != type) continue;

            foundIndex++;
            if (foundIndex == index) return Optional.of(result);
        }
        return Optional.empty();
    }

    private String toJsonString(Context context, int widgetId) {
        try {
            return toJson(context, widgetId, true).toString(2);
        } catch (JSONException e) {
            return "Error while formatting data " + e;
        }
    }

    public JSONObject toJson(Context context, int widgetId, boolean withSettings) throws JSONException {
        JSONArray resultsArray = new JSONArray();
        for (QueryResult result : results) {
            if (result.getWidgetId() == widgetId) {
                resultsArray.put(result.toJson());
            }
        }
        WidgetData widgetData = context == null || widgetId == 0
            ? WidgetData.EMPTY
            : WidgetData.fromSettings(context, withSettings ? AllSettings.instanceFromId(context, widgetId) : null);

        JSONObject json = widgetData.toJson();
        json.put(KEY_RESULTS_VERSION, RESULTS_VERSION);
        json.put(KEY_RESULTS, resultsArray);
        return json;
    }

    public static QueryResultsStorage fromJson(int widgetId, JSONObject jsonStorage) {
        QueryResultsStorage resultsStorage = new QueryResultsStorage();
        if (jsonStorage.has(KEY_RESULTS)) {
            try {
                JSONArray jsonResults = jsonStorage.getJSONArray(KEY_RESULTS);
                for (int ind = 0; ind < jsonResults.length(); ind++) {
                    resultsStorage.addResult(QueryResult.fromJson(jsonResults.getJSONObject(ind), widgetId));
                }
            } catch (Exception e) {
                Log.w(TAG, "Error reading results", e);
            }
        }
        return resultsStorage;
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

    public void clear() {
        results.clear();
        executedAt.set(null);
    }

    public void setExecutedAt(DateTime date) {
        executedAt.set(date);
    }

    public DateTime getExecutedAt() {
        return executedAt.get();
    }
}
