package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.plusonelabs.calendar.calendar.CalendarEvent;
import com.plusonelabs.calendar.calendar.CalendarQueryStoredResults;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.model.Event;
import com.plusonelabs.calendar.prefs.CalendarPreferences;
import com.plusonelabs.calendar.util.RawResourceUtils;

import org.joda.time.DateTime;
import org.json.JSONException;

import java.io.IOException;

/**
 * @author yvolk@yurivolkov.com
 */
public class BirthdayTest extends InstrumentationTestCase {
    private static final String TAG = BirthdayTest.class.getSimpleName();

    private MockCalendarContentProvider mProvider = null;
    private EventRemoteViewsFactory factory = null;
    private int eventRangeStored;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mProvider = MockCalendarContentProvider.getContentProvider(this);
        factory = new EventRemoteViewsFactory(mProvider.getContext());
        eventRangeStored = CalendarPreferences.getEventRange(mProvider.getContext());
    }

    public void testBirthdayOneDayOnly() throws IOException, JSONException {
        CalendarQueryStoredResults inputs = CalendarQueryStoredResults.fromJsonString(
                RawResourceUtils.getString(this.getInstrumentation().getContext(),
                        com.plusonelabs.calendar.tests.R.raw.birthday)
        );

        // TODO: In order for this to have effect, we should use Content provider,
        // which parses "selection" argument
        CalendarPreferences.setEventRange(mProvider.getContext(), 30);

        playAtOneTime(inputs, new DateTime(2015, 8, 1, 17,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 8, 17,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 8, 23, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9,  0, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9, 11,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9, 17,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9, 23, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 0, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10,11,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10,17,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10,23, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 11, 0, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 11,11,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 11,17,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 11,23, 30), 2);

        // TODO: This also doesn't work yet. We need to inject a Mock content provider deeper...
        mProvider.refreshWidget();
    }

    private void playAtOneTime(CalendarQueryStoredResults inputs, DateTime now, int numberOfEntriesExpected) {
        mProvider.addResults(inputs.getResults());
        DateUtil.setNow(now);
        factory.onDataSetChanged();
        for (int ind=0; ind < factory.getWidgetEntries().size(); ind++) {
            Event widgetEntry = factory.getWidgetEntries().get(ind);
            Log.v(TAG, String.format("%02d ", ind) + widgetEntry.toString());
        }
        assertEquals(numberOfEntriesExpected, factory.getWidgetEntries().size());
        if (numberOfEntriesExpected > 0) {
            CalendarEvent birthday = (CalendarEvent) factory.getWidgetEntries().get(1);
            assertEquals(9, birthday.getStartDay().dayOfMonth().get());
            assertEquals(0, birthday.getStartDay().hourOfDay().get());
            assertEquals(true, birthday.isAllDay());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        DateUtil.setNow(null);
        CalendarPreferences.setEventRange(mProvider.getContext(), eventRangeStored);
        mProvider.tearDown();
        super.tearDown();
    }
}
