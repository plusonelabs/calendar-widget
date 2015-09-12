package com.plusonelabs.calendar;

import com.plusonelabs.calendar.calendar.CalendarEvent;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.widget.CalendarEntry;
import com.plusonelabs.calendar.widget.WidgetEntry;

import android.test.InstrumentationTestCase;
import android.util.Log;

import org.joda.time.DateTime;

/**
 * @author yvolk@yurivolkov.com
 */
public class SingleEventTest extends InstrumentationTestCase {
    private static final String TAG = SingleEventTest.class.getSimpleName();

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

    public void testEventAttributes() {
        DateTime today = DateUtil.now().withTimeAtStartOfDay();
        DateUtil.setNow(today.plusHours(10));
        CalendarEvent event = new CalendarEvent();
        event.setEventId(++mEventId);
        event.setTitle("Single Event today with all known attributes");
        event.setStartDate(today.plusHours(12));
        event.setEndDate(today.plusHours(13));
        event.setColor(0xFF92E1C0);
        event.setAllDay(false);
        event.setLocation("somewhere");
        event.setAlarmActive(true);
        event.setRecurring(true);

        assertOneEvent(event, true);
        event.setAlarmActive(false);
        assertOneEvent(event, true);
        event.setRecurring(false);
        assertOneEvent(event, true);

        event.setAllDay(true);
        assertOneEvent(event, false);
        event.setStartDate(today);
        event.setEndDate(today.plusDays(1));
        assertOneEvent(event, true);
    }

    private void assertOneEvent(CalendarEvent event, boolean equal) {
        mProvider.clear();
        mProvider.addRow(event);
        mFactory.onDataSetChanged();
        for (WidgetEntry entry : mFactory.getWidgetEntries()) {
            Log.v(TAG, entry.toString());
        }
        assertEquals(1, mProvider.getQueriesCount());
        assertEquals(mFactory.getWidgetEntries().toString(), 2, mFactory.getWidgetEntries().size());
        WidgetEntry entry = mFactory.getWidgetEntries().get(1);
        assertTrue(entry instanceof CalendarEntry);
        CalendarEvent eventOut = ((CalendarEntry) entry).getEvent();
        if (equal) {
            assertEquals(event.toString(), eventOut.toString());
        }
        else {
            assertNotSame(event.toString(), eventOut.toString());
        }
    }
}
