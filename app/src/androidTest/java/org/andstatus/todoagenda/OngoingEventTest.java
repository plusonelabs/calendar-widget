package org.andstatus.todoagenda;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author yvolk@yurivolkov.com
 */
public class OngoingEventTest extends BaseWidgetTest {

    private int eventId = 0;

    /**
     * @see <a href="https://github.com/plusonelabs/calendar-widget/issues/199">Issue 199</a>
     */
    @Test
    public void testTodaysOngoingEvent() {
        DateTime today = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), false);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Ongoing event shows original start time");
        event.setStartDate(today.plusHours(9));
        event.setEndDate(today.plusHours(12));

        DateUtil.setNow(today.plusHours(10).plusMinutes(33));
        provider.addRow(event);
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
        CalendarEntry entry = null;
        for (WidgetEntry item : factory.getWidgetEntries()) {
            if (item instanceof CalendarEntry) {
                entry = (CalendarEntry) item;
            }
        }
        assertNotNull(entry);
        assertTrue("Is active event", entry.getEvent().isActive());
        assertFalse("Is not part of Multi Day Event", entry.isPartOfMultiDayEvent());
        assertEquals("Start Time didn't change for today's event", event.getStartDate(), entry.getStartDate());
        assertEquals("End Time didn't change for today's event", event.getEndDate(), entry.getEndDate());
    }

    /**
     * @see <a href="https://github.com/plusonelabs/calendar-widget/issues/199">Issue 199</a>
     */
    @Test
    public void testYesterdaysOngoingEvent() {
        DateTime today = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), false);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Ongoing event, which started yesterday, shows no start time");
        event.setStartDate(today.minusDays(1).plusHours(9));
        event.setEndDate(today.plusHours(12));

        DateUtil.setNow(today.plusHours(10).plusMinutes(33));
        provider.addRow(event);
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
        CalendarEntry entry = null;
        for (WidgetEntry item : factory.getWidgetEntries()) {
            if (item instanceof CalendarEntry) {
                entry = (CalendarEntry) item;
            }
        }
        assertNotNull(entry);
        assertTrue("Is active event", entry.getEvent().isActive());
        assertTrue("Is Part of Multi Day Event", entry.isPartOfMultiDayEvent());
        assertFalse("Is not start of Multi Day Event", entry.isStartOfMultiDayEvent());
        assertTrue("Is end of Multi Day Event", entry.isEndOfMultiDayEvent());
        assertEquals("Yesterday's event entry start time is midnight", today, entry.getStartDate());
        assertEquals("End Time didn't change for yesterday's event", event.getEndDate(), entry.getEndDate());
    }

    @Test
    public void testEventWhichCarryOverToTheNextDay() {
        DateTime today = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), false);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Event that carry over to the next day, show as ending midnight");
        event.setStartDate(today.plusHours(19));
        event.setEndDate(today.plusDays(1).plusHours(7));

        DateUtil.setNow(today.plusHours(20).plusMinutes(33));
        provider.addRow(event);
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
        CalendarEntry entry = null;
        for (WidgetEntry item : factory.getWidgetEntries()) {
            if (item instanceof CalendarEntry) {
                entry = (CalendarEntry) item;
                break;
            }
        }
        assertNotNull(entry);
        assertTrue("Is active event", entry.getEvent().isActive());
        assertTrue("Is Part of Multi Day Event", entry.isPartOfMultiDayEvent());
        assertTrue("Is start of Multi Day Event", entry.isStartOfMultiDayEvent());
        assertFalse("Is not an end of Multi Day Event", entry.isEndOfMultiDayEvent());
        assertEquals("Start Time didn't change for today's event", event.getStartDate(), entry.getStartDate());
        assertEquals("Event entry end time is next midnight", today.plusDays(1), entry.getEndDate());
    }

}
