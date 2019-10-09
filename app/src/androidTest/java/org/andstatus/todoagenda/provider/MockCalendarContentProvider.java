package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;
import android.util.Log;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.MockSettingsProvider;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.prefs.SettingsStorage;
import org.andstatus.todoagenda.testcompat.IsolatedContext;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.RawResourceUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.andstatus.todoagenda.prefs.AllSettings.getStorageKey;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_WIDGET_ID;
import static org.andstatus.todoagenda.provider.QueryResultsStorage.KEY_SETTINGS;

/**
 * @author yvolk@yurivolkov.com
 */
public class MockCalendarContentProvider extends MockContentProvider {

    final String TAG = this.getClass().getSimpleName();
    private static final int TEST_WIDGET_ID_MIN = 434892;
    private static final String[] ZONE_IDS = {"America/Los_Angeles", "Europe/Moscow", "Asia/Kuala_Lumpur", "UTC"};
    private volatile int queriesCount = 0;
    private final List<QueryResult> results = new CopyOnWriteArrayList<>();
    private final int numberOfOpenTaskSources;

    private final static AtomicInteger widgetId = new AtomicInteger(TEST_WIDGET_ID_MIN);

    public static MockCalendarContentProvider getContentProvider(int numberOfOpenTasksSources) throws JSONException {
        MockContentResolver mockResolver = new MockContentResolver();
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context isolatedContext = new IsolatedContext(mockResolver, targetContext);
        MockCalendarContentProvider contentProvider =
                new MockCalendarContentProvider(isolatedContext, numberOfOpenTasksSources);

        mockResolver.addProvider("com.android.calendar", contentProvider);
        if (numberOfOpenTasksSources > 0) {
            mockResolver.addProvider("org.dmfs.tasks", contentProvider);
        }
        mockResolver.addProvider("settings", new MockSettingsProvider());

        contentProvider.setPreferences(isolatedContext);
        return contentProvider;
    }

    private MockCalendarContentProvider(Context context, int numberOfOpenTaskSources) {
        super(context);
        this.numberOfOpenTaskSources = numberOfOpenTaskSources;
    }

    private void setPreferences(Context context) throws JSONException {
        DateTimeZone zone = DateTimeZone.forID(ZONE_IDS[(int)(System.currentTimeMillis() % ZONE_IDS.length)]);
        DateTimeZone.setDefault(zone);
        Log.i(getClass().getSimpleName(), "Default Time zone set to " + zone);

        InstanceSettings settings = AllSettings.instanceFromId(context, widgetId.incrementAndGet());
        JSONObject json = settings.toJson();
        JSONArray allSettingsJsonArray = new JSONArray();
        allSettingsJsonArray.put(json);
        AllSettings.loadFromTestData(context, allSettingsJsonArray);
    }

    public static void tearDown() {
        List<Integer> toDelete = new ArrayList<>();
        Map<Integer, InstanceSettings> instances = AllSettings.getLoadedInstances();
        for(InstanceSettings settings : instances.values()) {
            if (settings.getWidgetId() >= TEST_WIDGET_ID_MIN) {
                toDelete.add(settings.getWidgetId());
            }
        }
        for(int widgetId : toDelete) {
            instances.remove(widgetId);
            SettingsStorage.delete(ApplicationProvider.getApplicationContext(), getStorageKey(widgetId));
        }
        ApplicationPreferences.setWidgetId(ApplicationProvider.getApplicationContext(), TEST_WIDGET_ID_MIN);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if ("content://com.android.calendar/calendars".equals(uri.toString())) {
            Log.i(TAG, "query: Available Calendar sources");
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[]{1L, getClass().getSimpleName(), 0x00FF00, "my.test@example.com"});
            return cursor;
        }
        if ("content://org.dmfs.tasks/tasklists".equals(uri.toString())) {
            Log.i(TAG, "query: Available OpenTasks sources");
            if (numberOfOpenTaskSources == 0) return null;

            MatrixCursor cursor = new MatrixCursor(projection);
            for(int i = 0; i < numberOfOpenTaskSources; i++) {
                cursor.addRow(new Object[]{2 + i, getClass().getSimpleName() + ".task" + i, 0x0FF0000,
                        "my.task@example.com"});
            }
            return cursor;
        }
        if ("content://com.android.calendar/TasksAccounts".equals(uri.toString())) {
            Log.i(TAG, "query: Available Samsung task sources");
            return null;
        }

        Log.i(MockCalendarContentProvider.class.getSimpleName(), "query: " + uri);
        queriesCount++;
        if (results.size() < queriesCount) {
            return null;
        } else {
            return results.get(queriesCount - 1).query(projection);
        }
    }

    public void addResults(List<QueryResult> results) {
        for (QueryResult result : results) {
            addResult(result);
        }
        if (!results.isEmpty()) {
            Context context = getSettings().getContext();
            int widgetId = getSettings().getWidgetId();
            ApplicationPreferences.fromInstanceSettings(context, widgetId);
            ApplicationPreferences.setLockedTimeZoneId(context, results.get(0).getExecutedAt().getZone().getID());
            ApplicationPreferences.save(context, widgetId);
        }
    }

    public void addResult(QueryResult result) {
        results.add(result);
    }

    public void addRow(CalendarEvent event) {
        addRow(new QueryRow()
                .setCalendarId(event.getEventSource().source.getId())
                .setEventId(event.getEventId())
                .setTitle(event.getTitle())
                .setBegin(event.getStartMillis())
                .setEnd(event.getEndMillis())
                .setDisplayColor(event.getColor())
                .setAllDay(event.isAllDay() ? 1 : 0)
                .setEventLocation(event.getLocation())
                .setHasAlarm(event.isAlarmActive() ? 1 : 0)
                .setRRule(event.isRecurring() ? "FREQ=WEEKLY;WKST=MO;BYDAY=MO,WE,FR" : null)
        );
    }

    public void addRow(QueryRow queryRow) {
        if (results.isEmpty()) {
            addResult(new QueryResult(EventProviderType.CALENDAR, getSettings().getWidgetId(), DateUtil.now(getSettings().getTimeZone())));
        }
        results.get(0).addRow(queryRow);
    }

    @NonNull
    public InstanceSettings getSettings() {
        return AllSettings.instanceFromId(getContext(), getWidgetId());
    }

    public void clear() {
        queriesCount = 0;
        results.clear();
    }

    public int getQueriesCount() {
        return queriesCount;
    }

    public int getWidgetId() {
        return widgetId.get();
    }

    public void startEditing() {
        ApplicationPreferences.fromInstanceSettings(getContext(), getWidgetId());
    }

    public void saveSettings() {
        ApplicationPreferences.save(getContext(), getWidgetId());
    }

    public QueryResultsStorage loadResults(Context context, @RawRes int jsonResId)
            throws IOException, JSONException {
        JSONObject json = new JSONObject(RawResourceUtils.getString(context, jsonResId));
        json.getJSONObject(KEY_SETTINGS).put(PREF_WIDGET_ID, widgetId);
        return QueryResultsStorage.fromTestData(getContext(), json);
    }

    public OrderedEventSource getFirstActiveEventSource() {
        for(OrderedEventSource orderedSource: getSettings().getActiveEventSources()) {
            return orderedSource;
        }
        return OrderedEventSource.EMPTY;
    }
}
