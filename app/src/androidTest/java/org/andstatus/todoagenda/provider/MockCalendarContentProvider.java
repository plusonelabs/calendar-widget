package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.prefs.SettingsStorage;
import org.andstatus.todoagenda.prefs.SnapshotMode;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.RawResourceUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.andstatus.todoagenda.prefs.AllSettings.getStorageKey;
import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_WIDGET_ID;
import static org.andstatus.todoagenda.provider.QueryResultsStorage.KEY_SETTINGS;

/**
 * @author yvolk@yurivolkov.com
 */
public class MockCalendarContentProvider {
    final static String TAG = MockCalendarContentProvider.class.getSimpleName();
    private static final int TEST_WIDGET_ID_MIN = 434892;
    private static final String[] ZONE_IDS = {"America/Los_Angeles", "Europe/Moscow", "Asia/Kuala_Lumpur", "UTC"};
    private final QueryResultsStorage results = new QueryResultsStorage();
    private final Context context;

    private final static AtomicInteger lastWidgetId = new AtomicInteger(TEST_WIDGET_ID_MIN);
    private final int widgetId;
    private volatile InstanceSettings settings;

    public static MockCalendarContentProvider getContentProvider() {
        DateTimeZone zone = DateTimeZone.forID(ZONE_IDS[(int)(System.currentTimeMillis() % ZONE_IDS.length)]);
        DateTimeZone.setDefault(zone);
        Log.i(TAG, "Default Time zone set to " + zone);

        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MockCalendarContentProvider contentProvider = new MockCalendarContentProvider(targetContext);
        return contentProvider;
    }

    private MockCalendarContentProvider(Context context) {
        this.context = context;
        InstanceSettings instanceToReuse = AllSettings.getInstances(context).values().stream()
                .filter(settings -> settings.getWidgetInstanceName().endsWith(InstanceSettings.TEST_REPLAY_SUFFIX)).findFirst().orElse(null);

        widgetId = instanceToReuse == null ? lastWidgetId.incrementAndGet() : instanceToReuse.getWidgetId();
        settings = new InstanceSettings(context, widgetId,
                "ToDo Agenda " + widgetId + " " + InstanceSettings.TEST_REPLAY_SUFFIX);
        AllSettings.addNew(context, settings);
    }

    public void updateAppSettings() {
        InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
        if (!results.getResults().isEmpty()) {
            settings.setResultsStorage(results);
            settings.setSnapshotMode(SnapshotMode.SNAPSHOT_TIME);
        }
        AllSettings.addNew(context, settings);
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
        results.addResult(result);
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
        EventProviderType providerType = EventProviderType.CALENDAR;
        QueryResult result = results.findLast(providerType).orElseGet( () -> {
            QueryResult r2 = new QueryResult(providerType, getSettings().getWidgetId(),
                            DateUtil.now(getSettings().getTimeZone()));
            results.addResult(r2);
            return r2;
        });
        result.addRow(queryRow);
    }

    @NonNull
    public InstanceSettings getSettings() {
        return AllSettings.instanceFromId(getContext(), getWidgetId());
    }

    public void clear() {
        results.clear();
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void startEditing() {
        ApplicationPreferences.fromInstanceSettings(getContext(), getWidgetId());
    }

    public void saveSettings() {
        ApplicationPreferences.save(getContext(), getWidgetId());
    }

    public QueryResultsStorage loadResultsAndSettings(Context context, @RawRes int jsonResId)
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

    public Context getContext() {
        return context;
    }
}
