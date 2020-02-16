package org.andstatus.todoagenda;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @author yvolk@yurivolkov.com
 */
public class SingleEventTest extends BaseWidgetTest {

    private int eventId = 0;

    @Test
    public void testEventAttributes() {
        DateTime today = getSettings().clock().now().withTimeAtStartOfDay();

        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(), false);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Single Event today with all known attributes");
        event.setStartDate(today.plusHours(12));
        event.setEndDate(today.plusHours(13));
        event.setColor(0xFF92E1C0);
        event.setLocation("somewhere");
        event.setAlarmActive(true);
        event.setRecurring(true);

        DateTime executedAt = today.plusHours(10);
        assertOneEvent(executedAt, event, true);
        event.setAlarmActive(false);
        assertOneEvent(executedAt, event, true);
        event.setRecurring(false);
        assertOneEvent(executedAt, event, true);
    }

    @Test
    public void testAlldayEventAttributes() {
        DateTime today = getSettings().clock().now().withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(), true);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Single AllDay event today with all known attributes");
        event.setStartDate(today.minusDays(1));
        event.setEndDate(today.plusDays(1));
        event.setColor(0xFF92E1C0);
        event.setLocation("somewhere");

        DateTime executedAt = today.plusHours(10);
        assertOneEvent(executedAt, event, false);
        event.setStartDate(today);
        event.setEndDate(today.plusDays(1));
        assertOneEvent(executedAt, event, true);
    }


    @Test
    public void testAlldayEventMillis() {
        DateTime today = getSettings().clock().now(DateTimeZone.UTC).withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(), true);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Single All day event from millis");
        event.setStartMillis(today.getMillis());
        assertEquals(event.getStartDate().toString(), today.getMillis(), event.getStartMillis());
        assertEquals(event.getEndDate().toString(), today.plusDays(1).getMillis(), event.getEndMillis());
    }

    private void assertOneEvent(DateTime executedAt, CalendarEvent event, boolean equal) {
        provider.clear();
        provider.setExecutedAt(executedAt);
        provider.addRow(event);
        playResults(TAG);

        assertFalse(getSettings().toString(),
                getSettings().getActiveEventSources(EventProviderType.CALENDAR).isEmpty());
        OrderedEventSource source = provider.getFirstActiveEventSource();
        assertTrue(source.toString(), source.source.isAvailable);
        assertTrue(getSettings().toString(),
                getSettings().getActiveEventSource(EventProviderType.CALENDAR,
                        source.source.getId()).source.isAvailable);

        assertEquals(getFactory().getWidgetEntries().toString(), 3, getFactory().getWidgetEntries().size());
        WidgetEntry entry = getFactory().getWidgetEntries().get(1);
        assertTrue(entry instanceof CalendarEntry);
        CalendarEvent eventOut = ((CalendarEntry) entry).getEvent();
        String msgLog = "Comparing events:\n" +
                "in: " + event.toString() + "\n" +
                "out:" + eventOut.toString() + "\n";
        if (equal) {
            assertEquals(msgLog, event.toString(), eventOut.toString());
        } else {
            assertNotSame(msgLog, event.toString(), eventOut.toString());
        }
    }
}
