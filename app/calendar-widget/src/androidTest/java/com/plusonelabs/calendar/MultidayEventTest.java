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
public class MultidayEventTest extends InstrumentationTestCase {

    private static final String TAG = MultidayEventTest.class.getSimpleName();
    private static final String ARROW = "→";

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

    /**
     * Issue #206 https://github.com/plusonelabs/calendar-widget/issues/206
     */
    public void testEventWhichCarryOverToTheNextDay() {
        DateTime today = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), false);
        event.setEventId(++eventId);
        event.setTitle("Event that carry over to the next day, show as ending midnight");
        event.setStartDate(today.plusHours(19));
        event.setEndDate(today.plusDays(1).plusHours(7));

        DateUtil.setNow(today.plusHours(10).plusMinutes(33));
        provider.addRow(event);
        factory.onDataSetChanged();
        CalendarEntry entry1 = null;
        CalendarEntry entry2 = null;
        factory.logWidgetEntries(TAG);
        for (WidgetEntry item : factory.getWidgetEntries()) {
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
        assertEquals("Start Time didn't change for today's event", event.getStartDate(), entry1.getStartDate());
        assertEquals("Event entry end time is next midnight", today.plusDays(1), entry1.getEndDate());

        assertNotNull(entry2);
        assertFalse("Is not active event", entry2.getEvent().isActive());
        assertTrue("Is Part of Multi Day Event", entry2.isPartOfMultiDayEvent());
        assertFalse("Is not start of Multi Day Event", entry2.isStartOfMultiDayEvent());
        assertTrue("Is end of Multi Day Event", entry2.isEndOfMultiDayEvent());
        assertEquals("Start Time of tomorrow's entry is midnight", today.plusDays(1), entry2.getStartDate());
        assertEquals("Tomorrow event entry end time is the same as for the event", entry2.getEvent().getEndDate(), entry2.getEndDate());
    }

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/184#issuecomment-142671469
     */
    public void testThreeDaysEvent() {
        DateTime friday = new DateTime(2015, 9, 18, 0, 0, 0, 0, provider.getSettings().getTimeZone());
        DateTime sunday = friday.plusDays(2);
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), false);
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
        assertEquals(sunday, entry1.getStartDate());
        assertEquals(event.getEndDate(), entry1.getEndDate());
        assertEquals(event.getTitle(), entry1.getTitle());
        String timeString = entry1.getEventTimeString();
        assertTrue(timeString, timeString.contains(ARROW));
        assertEquals(timeString, timeString.indexOf(ARROW), timeString.lastIndexOf(ARROW));
    }

    private CalendarEntry getSundayEntryAt(CalendarEvent event, DateTime currentDateTime) {
        DateUtil.setNow(currentDateTime);
        provider.clear();
        provider.addRow(event);
        factory.onDataSetChanged();
        Log.i(TAG, "getSundayEntryAt " + currentDateTime);
        factory.logWidgetEntries(TAG);
        CalendarEntry sundayEntry = null;
        for (WidgetEntry item : factory.getWidgetEntries()) {
            if (item instanceof CalendarEntry) {
                CalendarEntry entry = (CalendarEntry) item;
                if (entry.getStartDate().getDayOfMonth() == 20) {
                    assertNull(sundayEntry);
                    sundayEntry = entry;
                }
            }
        }
        assertNotNull(sundayEntry);
        return sundayEntry;
    }
}
