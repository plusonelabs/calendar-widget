package org.andstatus.todoagenda;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author yvolk@yurivolkov.com
 */
public class MultidayEventTest extends BaseWidgetTest {

    private static final String ARROW = "→";

    private int eventId = 0;

    /**
     * Issue #206 https://github.com/plusonelabs/calendar-widget/issues/206
     */
    @Test
    public void testEventWhichCarryOverToTheNextDay() {
        DateTime today = getSettings().clock().now().withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(),false);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Event that carry over to the next day, show as ending midnight");
        event.setStartDate(today.plusHours(19));
        event.setEndDate(today.plusDays(1).plusHours(7));
        provider.setExecutedAt(today.plusHours(10).plusMinutes(33));
        provider.addRow(event);
        playResults(TAG);
        CalendarEntry entry1 = null;
        CalendarEntry entry2 = null;
        for (WidgetEntry item : getFactory().getWidgetEntries()) {
            if (item instanceof CalendarEntry) {
                if (entry1 == null) {
                    entry1 = (CalendarEntry) item;
                } else {
                    entry2 = (CalendarEntry) item;
                }
            }
        }
        assertNotNull(entry1);
        assertFalse("Is not active event", entry1.getEvent().isActive());
        assertTrue("Is Part of Multi Day Event", entry1.isPartOfMultiDayEvent());
        assertTrue("Is start of Multi Day Event", entry1.isStartOfMultiDayEvent());
        assertFalse("Is not an end of Multi Day Event", entry1.isEndOfMultiDayEvent());
        assertEquals("Start Time didn't change for today's event", event.getStartDate(), entry1.entryDate);
        assertEquals("Entry end time should be the same as Event end time", event.getEndDate(), entry1.endDate);

        assertNotNull(entry2);
        assertFalse("Is not active event", entry2.getEvent().isActive());
        assertTrue("Is Part of Multi Day Event", entry2.isPartOfMultiDayEvent());
        assertFalse("Is not start of Multi Day Event", entry2.isStartOfMultiDayEvent());
        assertTrue("Is end of Multi Day Event", entry2.isEndOfMultiDayEvent());
        assertEquals("Start Time of tomorrow's entry is midnight", today.plusDays(1), entry2.entryDate);
        assertEquals("Tomorrow event entry end time is the same as for the event", entry2.getEvent().getEndDate(), entry2.endDate);
    }

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/184#issuecomment-142671469
     */
    @Test
    public void testThreeDaysEvent() {
        DateTime friday = dateTime(2015, 9, 18);
        DateTime sunday = friday.plusDays(2);
        CalendarEvent event = new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(),false);
        event.setEventSource(provider.getFirstActiveEventSource());
        event.setEventId(++eventId);
        event.setTitle("Leader's weekend");
        event.setStartDate(friday.plusHours(19));
        event.setEndDate(sunday.plusHours(15));

        assertSundayEntryAt(event, sunday, friday.plusHours(14));
        assertSundayEntryAt(event, sunday, friday.plusDays(1).plusHours(14));
        assertSundayEntryAt(event, sunday, friday.plusDays(2).plusHours(14));
    }

    private void assertSundayEntryAt(CalendarEvent event, DateTime sunday, DateTime currentDateTime) {
        CalendarEntry entry1 = getSundayEntryAt(event, currentDateTime);
        assertEquals(sunday, entry1.entryDate);
        assertEquals(event.getEndDate(), entry1.endDate);
        assertEquals(event.getTitle(), entry1.getTitle());
        String timeString = entry1.getEventTimeString();
        assertTrue(timeString, timeString.contains(ARROW));
        assertEquals(timeString, timeString.indexOf(ARROW), timeString.lastIndexOf(ARROW));
    }

    private CalendarEntry getSundayEntryAt(CalendarEvent event, DateTime currentDateTime) {
        provider.clear();
        provider.setExecutedAt(currentDateTime);
        provider.addRow(event);
        playResults(TAG);
        CalendarEntry sundayEntry = null;
        for (WidgetEntry item : getFactory().getWidgetEntries()) {
            if (item instanceof CalendarEntry) {
                CalendarEntry entry = (CalendarEntry) item;
                if (entry.entryDate.getDayOfMonth() == 20) {
                    assertNull(sundayEntry);
                    sundayEntry = entry;
                }
            }
        }
        assertNotNull(sundayEntry);
        return sundayEntry;
    }
}
