package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;

import com.plusonelabs.calendar.calendar.CalendarQueryResultsStorage;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.util.RawResourceUtils;

import org.json.JSONException;

import java.io.IOException;

/**
 * @author yvolk@yurivolkov.com
 */
public class WrongDatesLostEventsTest extends InstrumentationTestCase {
    private static final String TAG = WrongDatesLostEventsTest.class.getSimpleName();

    private MockCalendarContentProvider provider = null;
    private EventRemoteViewsFactory factory = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = MockCalendarContentProvider.getContentProvider(this);
        factory = new EventRemoteViewsFactory(provider.getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
    }

    public void testInsidePeriod() throws IOException, JSONException {
        final String method = "testInsidePeriod";
        CalendarQueryResultsStorage inputs = CalendarQueryResultsStorage.fromJsonString(
                provider.getContext(),
                RawResourceUtils.getString(this.getInstrumentation().getContext(),
                        com.plusonelabs.calendar.tests.R.raw.wrong_dates_lost_events)
        );
        provider.addResults(inputs.getResults());

        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
    }
}
