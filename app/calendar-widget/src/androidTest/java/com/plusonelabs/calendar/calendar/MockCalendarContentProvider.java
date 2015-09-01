package com.plusonelabs.calendar.calendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yvolk@yurivolkov.com
 */
public class MockCalendarContentProvider extends MockContentProvider {

    private int mQueriesCount = 0;
    private final List<CalendarQueryResult> mResults = new ArrayList<>();
    private final Set<String> storedCalendars;
    private final JSONObject storedPreferences;

    public void refreshWidget() {
        Intent intent = new Intent(EventAppWidgetProvider.ACTION_REFRESH);
        getContext().sendBroadcast(intent);
    }

    public static MockCalendarContentProvider getContentProvider(InstrumentationTestCase testCase) throws JSONException {
        MockContentResolver mockResolver = new MockContentResolver();
        Context isolatedContext = new IsolatedContext(mockResolver, testCase.getInstrumentation().getTargetContext());
        MockCalendarContentProvider contentProvider = new MockCalendarContentProvider(isolatedContext);
        mockResolver.addProvider("com.android.calendar", contentProvider);
        return contentProvider;
    }

    public MockCalendarContentProvider(Context context) throws JSONException {
        super(context);
        storedCalendars = CalendarPreferences.getActiveCalendars(context);
        storedPreferences = CalendarPreferences.toJson(context);
        setPreferences(context);
    }

    void setPreferences(Context context) throws JSONException {
        Set<String> calendars = new HashSet<>();
        calendars.add("1");
        CalendarPreferences.setActiveCalendars(context, calendars);
        CalendarPreferences.fromJson(context,
                new JSONObject("{" +
                " \"showDaysWithoutEvents\": false," +
                " \"hideBasedOnKeywords\": \"\"," +
                " \"eventRange\": 30," +
                " \"showPastEventsWithDefaultColor\": false," +
                " \"fillAllDay\": true," +
                " \"eventsEnded\": \"\"" +
                "}"));
    }

    public void tearDown() throws JSONException {
        CalendarPreferences.setActiveCalendars(getContext(), storedCalendars);
        CalendarPreferences.fromJson(getContext(), storedPreferences);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        mQueriesCount++;
        if (mResults.size() < mQueriesCount) {
            return null;
        } else {
            return mResults.get(mQueriesCount - 1).query(uri, projection, selection, selectionArgs, sortOrder);
        }
    }

    public void addResults(List<CalendarQueryResult> results) {
        for (CalendarQueryResult result : results) {
            addResult(result);
        }
    }

    public void addResult(CalendarQueryResult result) {
        mResults.add(result);
    }

    public void addRow(CalendarQueryRow calendarQueryRow) {
        if(mResults.isEmpty()) {
            addResult(new CalendarQueryResult(DateUtil.now()));
        }
        mResults.get(0).addRow(calendarQueryRow);
    }

    public void clear() {
        mQueriesCount = 0;
        mResults.clear();
    }

    public int getQueriesCount() {
        return mQueriesCount;
    }
}
