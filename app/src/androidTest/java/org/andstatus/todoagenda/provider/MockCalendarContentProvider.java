package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.test.platform.app.InstrumentationRegistry;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.prefs.SettingsStorage;
import org.andstatus.todoagenda.prefs.SnapshotMode;
import org.andstatus.todoagenda.util.RawResourceUtils;
import org.joda.time.DateTime;
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
    public final boolean usesActualWidget;
    private volatile InstanceSettings settings;

    public static MockCalendarContentProvider getContentProvider() {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MockCalendarContentProvider contentProvider = new MockCalendarContentProvider(targetContext);
        return contentProvider;
    }

    private MockCalendarContentProvider(Context context) {
        this.context = context;
        InstanceSettings instanceToReuse = AllSettings.getInstances(context).values().stream()
                .filter(settings -> settings.getWidgetInstanceName().endsWith(InstanceSettings.TEST_REPLAY_SUFFIX)).findFirst().orElse(null);
        usesActualWidget = instanceToReuse != null;

        widgetId = usesActualWidget ? instanceToReuse.getWidgetId() : lastWidgetId.incrementAndGet();
        InstanceSettings settings = new InstanceSettings(context, widgetId,
                "ToDo Agenda " + widgetId + " " + InstanceSettings.TEST_REPLAY_SUFFIX);
        settings.setActiveEventSources(settings.getActiveEventSources());

        settings.clock().setLockedTimeZoneId(ZONE_IDS[(int)(System.currentTimeMillis() % ZONE_IDS.length)]);
        setSettings(settings);
    }

    private void setSettings(InstanceSettings settings) {
        this.settings = settings;
        AllSettings.addNew(TAG, context, settings);
    }

    public void updateAppSettings(String tag) {
        settings.setResultsStorage(results);
        if (!results.getResults().isEmpty()) {
            settings.clock().setSnapshotMode(SnapshotMode.SNAPSHOT_TIME);
        }
        AllSettings.addNew(tag, context, settings);
        if (results.getResults().size() > 0) {
            Log.d(tag, "Results executed at " + settings.clock().now());
        }
    }

    public static void tearDown() {
        List<Integer> toDelete = new ArrayList<>();
        Map<Integer, InstanceSettings> instances = AllSettings.getLoadedInstances();
        for(InstanceSettings settings : instances.values()) {
            if (settings.getWidgetId() >= TEST_WIDGET_ID_MIN) {
                toDelete.add(settings.getWidgetId());
            }
        }
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        for(int widgetId : toDelete) {
            instances.remove(widgetId);
            SettingsStorage.delete(context, getStorageKey(widgetId));
        }
        ApplicationPreferences.setWidgetId(context, TEST_WIDGET_ID_MIN);
    }

    public void addResults(QueryResultsStorage newResults) {
        results.addResults(newResults);
    }

    public void setExecutedAt(DateTime executedAt) {
        results.setExecutedAt(executedAt);
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
                    getSettings().clock().now());
            results.addResult(r2);
            return r2;
        });
        result.addRow(queryRow);
    }

    @NonNull
    public InstanceSettings getSettings() {
        return settings;
    }

    public void clear() {
        results.clear();
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void startEditingPreferences() {
        ApplicationPreferences.fromInstanceSettings(getContext(), getWidgetId());
    }

    public void savePreferences() {
        ApplicationPreferences.save(getContext(), getWidgetId());
        settings = AllSettings.instanceFromId(getContext(), getWidgetId());
    }

    public QueryResultsStorage loadResultsAndSettings(@RawRes int jsonResId)
            throws IOException, JSONException {
        JSONObject json = new JSONObject(RawResourceUtils.getString(InstrumentationRegistry.getInstrumentation().getContext(), jsonResId));
        json.getJSONObject(KEY_SETTINGS).put(PREF_WIDGET_ID, widgetId);

        WidgetData widgetData = WidgetData.fromJson(json);
        InstanceSettings settings = widgetData.getSettingsForWidget(context, this.settings, widgetId);
        setSettings(settings);
        return settings.getResultsStorage();
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
