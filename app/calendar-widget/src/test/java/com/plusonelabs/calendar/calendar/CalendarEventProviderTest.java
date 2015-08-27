package com.plusonelabs.calendar.calendar;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class CalendarEventProviderTest {
    private final DateTime T_START;
    private final DateTime T_START_PLUS_1;

    public CalendarEventProviderTest() {
        DateTime someDaysFromNow = new DateTime();
        T_START = someDaysFromNow.withTimeAtStartOfDay().plusDays(6);
        T_START_PLUS_1 = T_START.plusDays(1);
    }

    /**
     * Verify that all events have non-null original events as per
     * https://github.com/plusonelabs/calendar-widget/pull/180#issuecomment-132340031
     */
    @Test
    public void testExpandEventList() {
        CalendarEvent testEvent = new CalendarEvent();
        testEvent.setStartDate(T_START);
        testEvent.setEndDate(T_START_PLUS_1);

        List<CalendarEvent> input = new LinkedList<>();
        input.add(testEvent);

        CalendarEventProvider testMe = new CalendarEventProvider(null);
        List<CalendarEvent> result = testMe.expandEventList(input);
        Assert.assertTrue("Result must be non-empty: " + result.size(),
                result.size() > 0);
        for (CalendarEvent event : result) {
            Assert.assertNotNull(event.toString(), event.getOriginalEvent());
        }
    }
}
