package org.andstatus.todoagenda;

import android.util.Log;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.provider.QueryRow;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.andstatus.todoagenda.util.DateUtil.exactMinutesPlusMinutes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author yvolk@yurivolkov.com
 */
public class IllegalInstantDueToTimeZoneTransitionTest extends BaseWidgetTest {
    private static final String TAG = IllegalInstantDueToTimeZoneTransitionTest.class.getSimpleName();

    private int eventId = 0;

    /**
     * Issue 186
     * See http://joda-time.sourceforge.net/faq.html#illegalinstant
     * http://stackoverflow.com/questions/25233776/unable-to-create-a-specific-joda-datetime-illegalinstantexception
     * http://beust.com/weblog/2013/03/30/the-time-that-never-was/
     * <p>
     * I couldn't reproduce the problem though.
     */
    @Test
    public void testIllegalInstantDueToTimeZoneOffsetTransition() {
        reproducedTimeZoneOffsetTransitionException();
        oneTimeDst("2014-09-07T00:00:00+00:00");
        oneTimeDst("2015-03-29T00:00:00+00:00");
        oneTimeDst("2015-10-25T00:00:00+00:00");
        oneTimeDst("2011-03-27T00:00:00+00:00");
        oneTimeDst("1980-04-06T00:00:00+00:00");
        provider.addRow(new CalendarEvent(getSettings(), provider.getContext(), provider.getWidgetId(), false)
            .setStartDate(getSettings().clock().startOfTomorrow())
            .setEventSource(provider.getFirstActiveEventSource())
            .setTitle("This will be the only event that will be shown"));
        playResults(TAG);
        assertEquals(3, getFactory().getWidgetEntries().size());
    }

    private void oneTimeDst(String iso8601time) {
        long millis = toMillis(iso8601time);
        String title = "DST";
        for (int ind = -25; ind < 26; ind++) {
            provider.addRow(new QueryRow().setEventId(++eventId).setTitle(title + " " + ind)
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

    /** https://github.com/andstatus/todoagenda/issues/13  */
    @Test
    public void testPeriodicAlarmTimeDuringTimeGap() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        try {
            DateTimeZone zone = DateTimeZone.forID("America/Winnipeg");
            DateTimeZone.setDefault(zone);
            int periodMinutes = 10;

            DateTime nowUtc = new DateTime(2020, 3, 8, 2, 15,
                    DateTimeZone.UTC).plusSeconds(4);
            assertEquals(new DateTime(2020, 3, 8, 2, 15 + 1 + periodMinutes,
                    DateTimeZone.UTC),
                    exactMinutesPlusMinutes(nowUtc, periodMinutes));

            DateTime now1 = nowUtc.plusHours(5).withZone(zone);
            DateTime next1 = exactMinutesPlusMinutes(now1, periodMinutes);
            assertEquals("Next time: " + next1, 1, next1.getHourOfDay());

            DateTime now2 = nowUtc.plusHours(6).withZone(zone);
            DateTime next2 = exactMinutesPlusMinutes(now2, periodMinutes);
            assertEquals("Next time: " + next2, 3, next2.getHourOfDay());

            DateTime nowWinnipeg = new DateTime(2020, 3, 8,
                    1,
                    54, zone).plusSeconds(37);
            DateTime expWinnipeg = new DateTime(2020, 3, 8,
                    1 + 2,
                    54 + 1 + periodMinutes - 60, zone);
            assertEquals(expWinnipeg, exactMinutesPlusMinutes(nowWinnipeg, periodMinutes));
        } finally {
            DateTimeZone.setDefault(defaultZone);
        }
    }
}
