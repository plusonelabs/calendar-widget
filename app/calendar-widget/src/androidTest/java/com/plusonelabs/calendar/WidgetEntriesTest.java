package com.plusonelabs.calendar;

import com.plusonelabs.calendar.calendar.CalendarQueryRow;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.widget.WidgetEntry;

import android.test.InstrumentationTestCase;
import android.util.Log;

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
public class WidgetEntriesTest extends InstrumentationTestCase {
    private static final String TAG = WidgetEntriesTest.class.getSimpleName();

    private MockCalendarContentProvider mProvider = null;
    private EventRemoteViewsFactory factory = null;
    private long mEventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mProvider = MockCalendarContentProvider.getContentProvider(this);
        factory = new EventRemoteViewsFactory(mProvider.getContext());
        assertTrue(factory.getWidgetEntries().isEmpty());
        mEventId = 0;
    }

    public void testSingleEventToday() {
        mProvider.addRow(new CalendarQueryRow().setEventId(++mEventId)
                .setTitle("Single Event today").setBegin(System.currentTimeMillis()));
        factory.onDataSetChanged();
        assertEquals(1, mProvider.getQueriesCount());
        assertEquals(factory.getWidgetEntries().toString(), factory.getWidgetEntries().size(), 2);
    }

    /**
     * Issue 186
     * See http://joda-time.sourceforge.net/faq.html#illegalinstant
     * http://stackoverflow.com/questions/25233776/unable-to-create-a-specific-joda-datetime-illegalinstantexception
     * http://beust.com/weblog/2013/03/30/the-time-that-never-was/
     *
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
        for (WidgetEntry event : factory.getWidgetEntries()) {
            Log.v("TimeZoneOffsetTrans", event.toString());
        }
        assertEquals(1, mProvider.getQueriesCount());
    }

    private void oneTimeDst(String iso8601time) {
        long millis = toMillis(iso8601time);
        String title = "DST";
        for (int ind=-25; ind<26; ind++) {
            mProvider.addRow(new CalendarQueryRow().setEventId(++mEventId).setTitle(title + " " + ind)
                    .setBegin(millis + TimeUnit.HOURS.toMillis(ind)).setAllDay(1));
        }
    }

    /**
     * from http://stackoverflow.com/a/5451245/297710
     */
    private void reproducedTimeZoneOffsetTransitionException() {
        boolean exceptionReproduced = false;
        final DateTimeZone dtz = DateTimeZone.forID("CET");
        LocalDateTime ldt = new LocalDateTime(dtz)
                .withYear(2011)
                .withMonthOfYear(3)
                .withDayOfMonth(27)
                .withHourOfDay(2);

        // this is just here to illustrate I'm solving the problem;
        // don't need in operational code
        try {
            DateTime myDateBroken = ldt.toDateTime(dtz);
            fail("No exception for " + ldt + " -> " + myDateBroken);
        } catch (IllegalArgumentException iae) {
            Log.v(TAG, "Sure enough, invalid instant due to time zone offset transition: " + ldt);
        }

        if (dtz.isLocalDateTimeGap(ldt)) {
            ldt = ldt.withHourOfDay(3);
        }

        DateTime myDate = ldt.toDateTime(dtz);
        System.out.println("No problem with this date: " + myDate);
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

    public void testShowRecurringEvents() {
        DateTime date = DateTime.now().withTimeAtStartOfDay();
        long millis = date.getMillis() + TimeUnit.HOURS.toMillis(10);
        mEventId++;
        for (int ind=0; ind<15; ind++) {
            millis += TimeUnit.DAYS.toMillis(ind);
            mProvider.addRow(new CalendarQueryRow().setEventId(mEventId).setTitle("Work each day")
                    .setBegin(millis).setEnd(millis + TimeUnit.HOURS.toMillis(9)));
        }
        factory.onDataSetChanged();
        for (WidgetEntry event : factory.getWidgetEntries()) {
            Log.v("ShowRecurringEvents", event.toString());
        }
        assertTrue("Entries: " + factory.getWidgetEntries().size(), factory.getWidgetEntries().size() > 15);
    }

    @Override
    protected void tearDown() throws Exception {
        mProvider.tearDown();
        super.tearDown();
    }
}
