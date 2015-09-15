package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.plusonelabs.calendar.calendar.CalendarQueryRow;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

/**
 * @author yvolk@yurivolkov.com
 */
public class RecurringEventsTest extends InstrumentationTestCase {
    private static final String TAG = RecurringEventsTest.class.getSimpleName();

    private MockCalendarContentProvider provider = null;
    private EventRemoteViewsFactory factory = null;
    private int eventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = MockCalendarContentProvider.getContentProvider(this);
        factory = new EventRemoteViewsFactory(provider.getContext());
        assertTrue(factory.getWidgetEntries().isEmpty());
        eventId = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
    }

    /**
     * @see <a href="https://github.com/plusonelabs/calendar-widget/issues/191">Issue 191</a>
     */
    public void testShowRecurringEvents() {
        DateTime date = DateTime.now().withTimeAtStartOfDay();
        long millis = date.getMillis() + TimeUnit.HOURS.toMillis(10);
        eventId++;
        for (int ind=0; ind<15; ind++) {
            millis += TimeUnit.DAYS.toMillis(1);
            provider.addRow(new CalendarQueryRow().setEventId(eventId).setTitle("Work each day")
                    .setBegin(millis).setEnd(millis + TimeUnit.HOURS.toMillis(9)));
        }
        factory.onDataSetChanged();
        for (WidgetEntry entry : factory.getWidgetEntries()) {
            Log.v(TAG, entry.toString());
        }
        assertTrue("Entries: " + factory.getWidgetEntries().size(), factory.getWidgetEntries().size() > 15);
    }
}
