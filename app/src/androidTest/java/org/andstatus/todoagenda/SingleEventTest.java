package org.andstatus.todoagenda;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.andstatus.todoagenda.RemoteViewsFactory.MIN_MILLIS_BETWEEN_RELOADS;
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
        DateTime today = getSettings().clock().now(getSettings().getTimeZone()).withTimeAtStartOfDay();
        getSettings().clock().setNow(today.plusHours(10));
        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(),
                getSettings().getTimeZone(), false);
        event.setEventSource(provider.getFirstActiveEventSource());
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

    @Test
    public void testAlldayEventAttributes() {
        DateTime today = getSettings().clock().now(getSettings().getTimeZone()).withTimeAtStartOfDay();
        getSettings().clock().setNow(today.plusHours(10));
        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(),
                getSettings().getTimeZone(), true);
        event.setEventSource(provider.getFirstActiveEventSource());
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


    @Test
    public void testAlldayEventMillis() {
        EnvironmentChangedReceiver.sleep(MIN_MILLIS_BETWEEN_RELOADS);
        DateTime today = getSettings().clock().now(DateTimeZone.UTC).withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(),
                getSettings().getTimeZone(), true);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Single All day event from millis");
        event.setStartMillis(today.getMillis());
        assertEquals(event.getStartDate().toString(), today.getMillis(), event.getStartMillis());
        assertEquals(event.getEndDate().toString(), today.plusDays(1).getMillis(), event.getEndMillis());
    }

    private void assertOneEvent(CalendarEvent event, boolean equal) {
        EnvironmentChangedReceiver.sleep(MIN_MILLIS_BETWEEN_RELOADS);
        provider.clear();
        provider.addRow(event);
        playResults(TAG);

        assertFalse(getSettings().toString(),
                getSettings().getActiveEventSources(EventProviderType.CALENDAR).isEmpty());
        OrderedEventSource source = provider.getFirstActiveEventSource();
        assertTrue(source.toString(), source.source.isAvailable);
        assertTrue(getSettings().toString(),
                getSettings().getActiveEventSource(EventProviderType.CALENDAR,
                        source.source.getId()).source.isAvailable);

        assertEquals(factory.getWidgetEntries().toString(), 3, factory.getWidgetEntries().size());
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
