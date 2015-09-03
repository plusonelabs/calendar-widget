package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.plusonelabs.calendar.calendar.CalendarQueryStoredResults;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.prefs.CalendarPreferences;
import com.plusonelabs.calendar.util.RawResourceUtils;
import com.plusonelabs.calendar.widget.CalendarEntry;
import com.plusonelabs.calendar.widget.WidgetEntry;

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

        CalendarPreferences.setEventsEnded(mProvider.getContext(), EndedSometimeAgo.NONE);
        CalendarPreferences.setShowPastEventsWithDefaultColor(mProvider.getContext(), false);
        CalendarPreferences.setEventRange(mProvider.getContext(), 30);
        playAtOneTime(inputs, new DateTime(2015, 8, 1, 17, 0), 0);
        playAtOneTime(inputs, new DateTime(2015, 8,  9, 23, 59), 0);
        playAtOneTime(inputs, new DateTime(2015, 8, 10,  0,  0), 0);
        playAtOneTime(inputs, new DateTime(2015, 8, 10,  0,  1), 2);

        playAtOneTime(inputs, new DateTime(2015, 9, 8, 17,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 8, 23, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9,  0, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9, 11,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9, 17,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 9, 23, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 0, 30), 0);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 11, 0), 0);

        CalendarPreferences.setEventsEnded(mProvider.getContext(), EndedSometimeAgo.ONE_HOUR);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 0, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 1, 30), 0);

        CalendarPreferences.setEventsEnded(mProvider.getContext(), EndedSometimeAgo.TODAY);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 1, 30), 0);

        CalendarPreferences.setEventsEnded(mProvider.getContext(), EndedSometimeAgo.FOUR_HOURS);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 1, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 3, 59), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 4,  0), 0);

        CalendarPreferences.setEventsEnded(mProvider.getContext(), EndedSometimeAgo.YESTERDAY);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 4, 0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10,11,  0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10, 17, 0), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 10,23, 30), 2);
        playAtOneTime(inputs, new DateTime(2015, 9, 11, 0,  0), 0);
        playAtOneTime(inputs, new DateTime(2015, 9, 11, 0, 30), 0);

        CalendarPreferences.setShowPastEventsWithDefaultColor(mProvider.getContext(), true);
        playAtOneTime(inputs, new DateTime(2015, 9, 11, 0, 30), 0);

        // TODO: This doesn't work yet. We need to inject a MockCalendarContentProvider deeper,
        // so it could work in a normal Context also
        mProvider.refreshWidget();
    }

    private void playAtOneTime(CalendarQueryStoredResults inputs, DateTime now, int numberOfEntriesExpected) {
        mProvider.addResults(inputs.getResults());
        DateUtil.setNow(now);
        factory.onDataSetChanged();
        for (int ind=0; ind < factory.getWidgetEntries().size(); ind++) {
            WidgetEntry widgetEntry = factory.getWidgetEntries().get(ind);
            Log.v(TAG, String.format("%02d ", ind) + widgetEntry.toString());
        }
        assertEquals(numberOfEntriesExpected, factory.getWidgetEntries().size());
        if (numberOfEntriesExpected > 0) {
            CalendarEntry birthday = (CalendarEntry) factory.getWidgetEntries().get(1);
            assertEquals(9, birthday.getStartDate().dayOfMonth().get());
            assertEquals(0, birthday.getStartDate().hourOfDay().get());
            assertEquals(0, birthday.getStartDate().minuteOfHour().get());
            assertEquals(0, birthday.getStartDate().millisOfDay().get());
            assertEquals(true, birthday.isAllDay());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        CalendarPreferences.setEventRange(mProvider.getContext(), eventRangeStored);
        mProvider.tearDown();
        super.tearDown();
    }
}
