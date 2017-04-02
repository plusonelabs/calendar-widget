package com.plusonelabs.calendar.calendar;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.prefs.InstanceSettings;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author yvolk@yurivolkov.com
 */
public class MockCalendarContentProvider extends MockContentProvider {

    private static final int WIDGET_ID = 434892;
    private int queriesCount = 0;
    private final List<CalendarQueryResult> results = new ArrayList<>();
    private final JSONArray storedSettings;
    private final DateTimeZone storedZone;

    private int widgetId = WIDGET_ID;

    private static DateTime fixDateForCalendar(DateTime date, boolean isAllDay) {
        if (!isAllDay) {
            return date;
        }
        return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
                date.getHourOfDay(), date.getMinuteOfHour(),
                DateTimeZone.UTC);
    }

    public static MockCalendarContentProvider getContentProvider(InstrumentationTestCase testCase) throws JSONException {
        MockContentResolver mockResolver = new MockContentResolver();
        Context isolatedContext = new IsolatedContext(mockResolver, testCase.getInstrumentation().getTargetContext());
        MockCalendarContentProvider contentProvider = new MockCalendarContentProvider(isolatedContext);
        mockResolver.addProvider("com.android.calendar", contentProvider);
        mockResolver.addProvider("settings", new MockSettingsProvider());
        return contentProvider;
    }

    public MockCalendarContentProvider(Context context) throws JSONException {
        super(context);
        storedSettings = InstanceSettings.toJson(getBaseContext(context));
        storedZone = DateTimeZone.getDefault();
        setPreferences(context);
    }

    static Context getBaseContext(Context context) {
        return ContextWrapper.class.isAssignableFrom(context.getClass()) ? ((ContextWrapper)
                context).getBaseContext() : context;
    }

    private void setPreferences(Context context) throws JSONException {
        while (InstanceSettings.getInstances(context).containsKey(widgetId) ||
               InstanceSettings.getInstances(context).containsKey(widgetId + 1)) {
            widgetId++;
        }
        if (InstanceSettings.getInstances(context).isEmpty()) {
            InstanceSettings settings1 = InstanceSettings.fromId(context, widgetId);
            assertFalse(settings1.isJustCreated());
        }
        widgetId++;
        InstanceSettings settings = InstanceSettings.fromId(context, widgetId);
        assertTrue(settings.isJustCreated());
        JSONObject json = settings.toJson();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json);
        InstanceSettings.fromJson(context, jsonArray);
    }

    public void tearDown() throws JSONException {
        InstanceSettings.delete(getContext(), widgetId - 1);
        InstanceSettings.delete(getContext(), widgetId);
        InstanceSettings.fromJson(getBaseContext(getContext()), storedSettings);
        DateUtil.setNow(null);
        DateTimeZone.setDefault(storedZone);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        queriesCount++;
        if (results.size() < queriesCount) {
            return null;
        } else {
            return results.get(queriesCount - 1).query(projection);
        }
    }

    public void addResults(List<CalendarQueryResult> results) {
        for (CalendarQueryResult result : results) {
            addResult(result);
        }
    }

    public void addResult(CalendarQueryResult result) {
        results.add(result);
    }

    public void addRow(CalendarEvent event) {
        addRow(new CalendarQueryRow()
                        .setEventId(event.getEventId())
                        .setTitle(event.getTitle())
                        .setBegin(fixDateForCalendar(event.getStartDate(), event.isAllDay()).getMillis())
                        .setEnd(fixDateForCalendar(event.getEndDate(), event.isAllDay()).getMillis())
                        .setDisplayColor(event.getColor())
                        .setAllDay(event.isAllDay() ? 1 : 0)
                        .setEventLocation(event.getLocation())
                        .setHasAlarm(event.isAlarmActive() ? 1 : 0)
                        .setRRule(event.isRecurring() ? "FREQ=WEEKLY;WKST=MO;BYDAY=MO,WE,FR" : null)
        );
    }

    public void addRow(CalendarQueryRow calendarQueryRow) {
        if(results.isEmpty()) {
            addResult(new CalendarQueryResult(DateUtil.now()));
        }
        results.get(0).addRow(calendarQueryRow);
    }

    public void clear() {
        queriesCount = 0;
        results.clear();
    }

    public int getQueriesCount() {
        return queriesCount;
    }

    public void refreshWidget() {
        Intent intent = new Intent(EventAppWidgetProvider.ACTION_REFRESH);
        getContext().sendBroadcast(intent);
    }

    public int getWidgetId() {
        return widgetId;
    }

}
