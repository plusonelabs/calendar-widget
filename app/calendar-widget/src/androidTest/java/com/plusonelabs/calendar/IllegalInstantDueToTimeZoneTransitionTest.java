package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.plusonelabs.calendar.calendar.CalendarQueryRow;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author yvolk@yurivolkov.com
 */
public class IllegalInstantDueToTimeZoneTransitionTest extends InstrumentationTestCase {

    private static final String TAG = IllegalInstantDueToTimeZoneTransitionTest.class.getSimpleName();

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
     * Issue 186
     * See http://joda-time.sourceforge.net/faq.html#illegalinstant
     * http://stackoverflow.com/questions/25233776/unable-to-create-a-specific-joda-datetime-illegalinstantexception
     * http://beust.com/weblog/2013/03/30/the-time-that-never-was/
     * <p>
     * I couldn't reproduce the problem though.
     */
    public void testIllegalInstantDueToTimeZoneOffsetTransition() {
        reproducedTimeZoneOffsetTransitionException();
        oneTimeDst("2014-09-07T00:00:00+00:00");
        oneTimeDst("2015-03-29T00:00:00+00:00");
        oneTimeDst("2015-10-25T00:00:00+00:00");
        oneTimeDst("2011-03-27T00:00:00+00:00");
        oneTimeDst("1980-04-06T00:00:00+00:00");
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
        assertEquals(1, provider.getQueriesCount());
    }

    private void oneTimeDst(String iso8601time) {
        long millis = toMillis(iso8601time);
        String title = "DST";
        for (int ind = -25; ind < 26; ind++) {
            provider.addRow(new CalendarQueryRow().setEventId(++eventId).setTitle(title + " " + ind)
                    .setBegin(millis + TimeUnit.HOURS.toMillis(ind)).setAllDay(1));
        }
    }

    /**
     * from http://stackoverflow.com/a/5451245/297710
     */
    private void reproducedTimeZoneOffsetTransitionException() {
        final DateTimeZone dateTimeZone = DateTimeZone.forID("CET");
        LocalDateTime localDateTime = new LocalDateTime(dateTimeZone)
                .withYear(2011)
                .withMonthOfYear(3)
                .withDayOfMonth(27)
                .withHourOfDay(2);

        // this is just here to illustrate I'm solving the problem;
        // don't need in operational code
        try {
            DateTime myDateBroken = localDateTime.toDateTime(dateTimeZone);
            fail("No exception for " + localDateTime + " -> " + myDateBroken);
        } catch (IllegalArgumentException iae) {
            Log.v(TAG, "Sure enough, invalid instant due to time zone offset transition: "
                    + localDateTime);
        }

        if (dateTimeZone.isLocalDateTimeGap(localDateTime)) {
            localDateTime = localDateTime.withHourOfDay(3);
        }

        DateTime myDate = localDateTime.toDateTime(dateTimeZone);
        Log.v(TAG, "No problem with this date: " + myDate);
    }

    /**
     * http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */
    private long toMillis(String iso8601time) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.GERMANY).parse(iso8601time);
        } catch (ParseException e) {
            throw new IllegalArgumentException(iso8601time, e);
        }
        return date.getTime();
    }
}
