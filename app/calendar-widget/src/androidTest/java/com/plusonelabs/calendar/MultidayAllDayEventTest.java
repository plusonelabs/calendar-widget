package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.plusonelabs.calendar.calendar.CalendarQueryResultsStorage;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.prefs.ApplicationPreferences;
import com.plusonelabs.calendar.widget.DayHeader;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;
import org.json.JSONException;

import java.io.IOException;

/**
 * @author yvolk@yurivolkov.com
 */
public class MultidayAllDayEventTest extends InstrumentationTestCase {

    private static final String TAG = MultidayAllDayEventTest.class.getSimpleName();

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

    public void testInsidePeriod() throws IOException, JSONException {
        final String method = "testInsidePeriod";
        CalendarQueryResultsStorage inputs = provider.loadResults(this.getInstrumentation().getContext(),
                com.plusonelabs.calendar.tests.R.raw.multi_day);
        provider.addResults(inputs.getResults());

        int dateRange = 30;
        provider.startEditing();
        ApplicationPreferences.setEventRange(provider.getContext(), dateRange);
        provider.saveSettings();
        DateTime now = new DateTime(2015, 8, 30, 0, 0, 1, 0, provider.getSettings().getTimeZone());
        DateUtil.setNow(now);
        factory.onDataSetChanged();
        factory.logWidgetEntries(method);

        DateTime today = now.withTimeAtStartOfDay();
        DateTime endOfRangeTime = today.plusDays(dateRange);
        int dayOfEventEntryPrev = 0;
        int dayOfHeaderPrev = 0;
        for (int ind = 0; ind < factory.getWidgetEntries().size(); ind++) {
            WidgetEntry entry = factory.getWidgetEntries().get(ind);
            String logMsg = method + "; " + String.format("%02d ", ind) + entry.toString();
            Log.v(TAG, logMsg);
            if (entry.getStartDay().isBefore(today)) {
                fail("Is present before today " + logMsg);
            }
            if (entry.getStartDay().isAfter(endOfRangeTime)) {
                fail("After end of range " + logMsg);
            }
            int dayOfEntry = entry.getStartDay().getDayOfYear();
            if (entry instanceof DayHeader) {
                if (dayOfHeaderPrev == 0) {
                    if (entry.getStartDate().withTimeAtStartOfDay().isAfter(today)) {
                        fail("No today's header " + logMsg);
                    }
                } else {
                    assertEquals("No header " + logMsg, dayOfHeaderPrev + 1, dayOfEntry);
                }
                dayOfHeaderPrev = dayOfEntry;
            } else {
                if (dayOfEventEntryPrev == 0) {
                    if (entry.getStartDate().withTimeAtStartOfDay().isAfter(today)) {
                        fail("Today not filled " + logMsg);
                    }
                } else {
                    assertEquals("Day not filled " + logMsg, dayOfEventEntryPrev + 1, dayOfEntry);
                }
                dayOfEventEntryPrev = dayOfEntry;
            }
        }
        assertEquals("Wrong last day header " + method, endOfRangeTime.getDayOfYear(), dayOfHeaderPrev);
        assertEquals("Wrong last filled day " + method, endOfRangeTime.getDayOfYear(), dayOfEventEntryPrev);
    }
}
