package com.plusonelabs.calendar.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Locale;

import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_DATE_FORMAT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_DATE_FORMAT_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_FILL_ALL_DAY;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_FILL_ALL_DAY_DEFAULT;

/**
 * Tests for {@link CalendarEventVisualizer}.
 *
 * @see <a href="http://tools.android.com/tech-docs/unit-testing-support">Android Tools Project Unit
 * Testing Support</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PreferenceManager.class, Log.class, DateUtils.class})
public class CalendarEventVisualizerTest {
    private static final DateTime T_2015_JUL_9 = new DateTime(2015, 7, 9, 0, 0);
    private static final DateTime T_2015_JUL_15 = new DateTime(2015, 7, 15, 0, 0);

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
    }

    /**
     * Mock of {@link DateUtils#formatDateTime(Context, long, int)}.
     */
    private String formatDateTime(long msSinceEpoch, int flags) {
        if (flags == (DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR)) {
            return DateTimeFormat.
                    forPattern("HH:mm").
                    withLocale(Locale.ENGLISH).
                    print(msSinceEpoch);
        }

        if (flags == (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY)) {
            return DateTimeFormat.
                    forPattern("EEE, MMM d").
                    withLocale(Locale.ENGLISH).
                    print(msSinceEpoch);
        }

        throw new IllegalArgumentException("Flags not supported by mock: " + flags);
    }

    private void mockPreferences(Context context, boolean fillAllDayEvents) {
        SharedPreferences prefs;
        PowerMockito.mockStatic(PreferenceManager.class);

        prefs = Mockito.mock(SharedPreferences.class);
        Mockito.when(prefs.getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT)).
                thenReturn(fillAllDayEvents);
        Mockito.when(prefs.getString(PREF_DATE_FORMAT, PREF_DATE_FORMAT_DEFAULT)).
                thenReturn("24");

        Mockito.when(PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(prefs);
    }

    private void mockDateUtils() {
        PowerMockito.mockStatic(DateUtils.class);
        Mockito.when(DateUtils.formatDateTime(Mockito.any(Context.class), Mockito.anyLong(), Mockito.anyInt())).
                thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) {
                        int flags = (int) invocation.getArguments()[2];
                        long msSinceEpoch = (long) invocation.getArguments()[1];
                        return formatDateTime(msSinceEpoch, flags);
                    }
                });
    }

    /**
     * Wrapper around {@link CalendarEventVisualizer#createTimeSpanString(CalendarEvent)} that sets
     * up preferences, a context and DateUtils properly before calling the actual method we want to
     * test.
     */
    private String createTimeSpanString(CalendarEvent event, boolean fillAllDayEvents) {
        Context context = Mockito.mock(Context.class);
        mockPreferences(context, fillAllDayEvents);
        mockDateUtils();

        CalendarEventVisualizer testMe = new CalendarEventVisualizer(context);
        return testMe.createTimeSpanString(event);
    }

    @Test
    public void testCreateTimeSpanStringNoFill() throws Exception {
        CalendarEvent multiDayAllDay = new CalendarEvent();
        multiDayAllDay.setAllDay(true);
        multiDayAllDay.setStartDate(T_2015_JUL_9);
        multiDayAllDay.setEndDate(T_2015_JUL_15);

        Assert.assertEquals("→ Tue, Jul 14", createTimeSpanString(multiDayAllDay, false));
    }

    @Test
    public void testCreateTimeSpanStringFill() throws Exception {
        CalendarEvent multiDayAllDay = new CalendarEvent();
        multiDayAllDay.setAllDay(true);
        multiDayAllDay.setStartDate(T_2015_JUL_9);
        multiDayAllDay.setEndDate(T_2015_JUL_15);

        Assert.assertEquals("00:00", createTimeSpanString(multiDayAllDay, true));
    }
}
