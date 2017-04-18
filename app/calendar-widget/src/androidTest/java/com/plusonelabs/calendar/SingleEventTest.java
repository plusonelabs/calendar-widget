package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;

import com.plusonelabs.calendar.calendar.CalendarEvent;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.widget.CalendarEntry;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
        factory = new EventRemoteViewsFactory(provider.getContext(), provider.getWidgetId());
        assertTrue(factory.getWidgetEntries().isEmpty());
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
    }

    public void testEventAttributes() {
        DateTime today = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
        DateUtil.setNow(today.plusHours(10));
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), false);
        event.setEventId(++eventId);
        event.setTitle("Single Event today with all known attributes");
        event.setStartDate(today.plusHours(12));
        event.setEndDate(today.plusHours(13));
        event.setColor(0xFF92E1C0);
        event.setLocation("somewhere");
        event.setAlarmActive(true);
        event.setRecurring(true);

        assertOneEvent(event, true);
        event.setAlarmActive(false);
        assertOneEvent(event, true);
        event.setRecurring(false);
        assertOneEvent(event, true);
    }

    public void testAlldayEventAttributes() {
        DateTime today = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
        DateUtil.setNow(today.plusHours(10));
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), true);
        event.setEventId(++eventId);
        event.setTitle("Single AllDay event today with all known attributes");
        event.setStartDate(today.minusDays(1));
        event.setEndDate(today.plusDays(1));
        event.setColor(0xFF92E1C0);
        event.setLocation("somewhere");
        assertOneEvent(event, false);
        event.setStartDate(today);
        event.setEndDate(today.plusDays(1));
        assertOneEvent(event, true);
    }


    public void testAlldayEventMillis() {
        DateTime today = DateUtil.now(DateTimeZone.UTC).withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), true);
        event.setEventId(++eventId);
        event.setTitle("Single All day event from millis");
        event.setStartMillis(today.getMillis());
        assertEquals(event.getStartDate().toString(), today.getMillis(), event.getStartMillis());
        assertEquals(event.getEndDate().toString(), today.plusDays(1).getMillis(), event.getEndMillis());
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
        } else {
            assertNotSame(event.toString(), eventOut.toString());
        }
    }
}
