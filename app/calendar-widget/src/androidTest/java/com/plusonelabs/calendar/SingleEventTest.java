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

    private MockCalendarContentProvider provider = null;
    private EventRemoteViewsFactory factory = null;
    private int eventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = MockCalendarContentProvider.getContentProvider(this);
        factory = new EventRemoteViewsFactory(provider.getContext());
        assertTrue(factory.getWidgetEntries().isEmpty());
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
    }

    public void testEventAttributes() {
        DateTime today = DateUtil.now().withTimeAtStartOfDay();
        DateUtil.setNow(today.plusHours(10));
        CalendarEvent event = new CalendarEvent();
        event.setEventId(++eventId);
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
        provider.clear();
        provider.addRow(event);
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
        assertEquals(1, provider.getQueriesCount());
        assertEquals(factory.getWidgetEntries().toString(), 2, factory.getWidgetEntries().size());
        WidgetEntry entry = factory.getWidgetEntries().get(1);
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
