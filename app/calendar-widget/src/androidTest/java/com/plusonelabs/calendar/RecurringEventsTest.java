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

    private MockCalendarContentProvider mProvider = null;
    private EventRemoteViewsFactory mFactory = null;
    private int mEventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mProvider = MockCalendarContentProvider.getContentProvider(this);
        mFactory = new EventRemoteViewsFactory(mProvider.getContext());
        assertTrue(mFactory.getWidgetEntries().isEmpty());
        mEventId = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        mProvider.tearDown();
        super.tearDown();
    }

    /**
     * @see <a href="https://github.com/plusonelabs/calendar-widget/issues/191">Issue 191</a>
     */
    public void testShowRecurringEvents() {
        DateTime date = DateTime.now().withTimeAtStartOfDay();
        long millis = date.getMillis() + TimeUnit.HOURS.toMillis(10);
        mEventId++;
        for (int ind=0; ind<15; ind++) {
            millis += TimeUnit.DAYS.toMillis(ind);
            mProvider.addRow(new CalendarQueryRow().setEventId(mEventId).setTitle("Work each day")
                    .setBegin(millis).setEnd(millis + TimeUnit.HOURS.toMillis(9)));
        }
        mFactory.onDataSetChanged();
        for (WidgetEntry entry : mFactory.getWidgetEntries()) {
            Log.v(TAG, entry.toString());
        }
        assertTrue("Entries: " + mFactory.getWidgetEntries().size(), mFactory.getWidgetEntries().size() > 15);
    }
}
