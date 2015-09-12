package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.plusonelabs.calendar.calendar.CalendarEvent;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.widget.CalendarEntry;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;

/**
 * @author yvolk@yurivolkov.com
 */
public class OngoingEventTest extends InstrumentationTestCase {
    private static final String TAG = OngoingEventTest.class.getSimpleName();

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
     * @see <a href="https://github.com/plusonelabs/calendar-widget/issues/199">Issue 199</a>
     */
    public void testTodaysOngoingEvent() {
        DateTime today = DateUtil.now().withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent();
        event.setEventId(++mEventId);
        event.setTitle("Ongoing event shows original start time");
        event.setStartDate(today.plusHours(9));
        event.setEndDate(today.plusHours(12));

        DateUtil.setNow(today.plusHours(10).plusMinutes(33));
        mProvider.clear();
        mProvider.addRow(event);
        mFactory.onDataSetChanged();
        CalendarEntry entry = null;
        for (WidgetEntry item : mFactory.getWidgetEntries()) {
            Log.v(TAG, item.toString());
            if (item instanceof CalendarEntry) {
                entry = (CalendarEntry) item;
            }
        }
        assertNotNull(entry);
        assertFalse("Is not part of Multi Day Event", entry.isPartOfMultiDayEvent());
        assertEquals("Start Time didn't change for today's event", event.getStartDate(), entry.getStartDate());
        assertEquals("End Time didn't change for today's event", event.getEndDate(), entry.getEndDate());
    }

    /**
     * @see <a href="https://github.com/plusonelabs/calendar-widget/issues/199">Issue 199</a>
     */
    public void testYesterdaysOngoingEvent() {
        DateTime today = DateUtil.now().withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent();
        event.setEventId(++mEventId);
        event.setTitle("Ongoing event, which started yesterday, shows no start time");
        event.setStartDate(today.minusDays(1).plusHours(9));
        event.setEndDate(today.plusHours(12));

        DateUtil.setNow(today.plusHours(10).plusMinutes(33));
        mProvider.clear();
        mProvider.addRow(event);
        mFactory.onDataSetChanged();
        CalendarEntry entry = null;
        for (WidgetEntry item : mFactory.getWidgetEntries()) {
            Log.v(TAG, item.toString());
            if (item instanceof CalendarEntry) {
                entry = (CalendarEntry) item;
            }
        }
        assertNotNull(entry);
        assertFalse("Is not start of Multi Day Event", entry.isStartOfMultiDayEvent());
        assertTrue("Is Part of Multi Day Event", entry.isPartOfMultiDayEvent());
        assertTrue("Is end of Multi Day Event", entry.isEndOfMultiDayEvent());
        assertEquals("Yesterday's event entry start time is midnight", today, entry.getStartDate());
        assertEquals("End Time didn't change for yesterday's event", event.getEndDate(), entry.getEndDate());
    }
}
