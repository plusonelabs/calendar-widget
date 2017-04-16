package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.plusonelabs.calendar.calendar.CalendarQueryResultsStorage;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.widget.CalendarEntry;

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
        factory = new EventRemoteViewsFactory(provider.getContext(), provider.getWidgetId());
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
    }

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/205
     */
    public void testIssue205() throws IOException, JSONException {
        final String method = "testIssue205";
        CalendarQueryResultsStorage inputs = provider.loadResults(this.getInstrumentation().getContext(),
                com.plusonelabs.calendar.tests.R.raw.wrong_dates_lost_events);
        provider.addResults(inputs.getResults());
        Log.d(method, "Results executed at " + inputs.getResults().get(0).getExecutedAt());

        factory.onDataSetChanged();
        factory.logWidgetEntries(method);
        assertEquals("Number of entries", 10, factory.getWidgetEntries().size());
        assertEquals("On Saturday", "Maker Fair", ((CalendarEntry) factory.getWidgetEntries().get(4)).getEvent().getTitle());
        assertEquals("On Saturday", 6, factory.getWidgetEntries().get(4).getStartDate().getDayOfWeek());
        assertEquals("On Sunday", "Ribakovs", ((CalendarEntry) factory.getWidgetEntries().get(7)).getEvent().getTitle());
        assertEquals("On Sunday", 7, factory.getWidgetEntries().get(7).getStartDate().getDayOfWeek());
    }
}
