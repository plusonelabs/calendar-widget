package com.plusonelabs.calendar.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;

import com.plusonelabs.calendar.PastTime;
import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.graphics.Color.*;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.*;

public class CalendarEventProvider {

    private static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC ";
    private static final String EVENT_SELECTION = Instances.SELF_ATTENDEE_STATUS + " != "
            + Attendees.ATTENDEE_STATUS_DECLINED;
    private static final String[] PROJECTION_4_0 = new String[]{Instances.EVENT_ID, Instances.TITLE,
            Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.EVENT_LOCATION,
            Instances.HAS_ALARM, Instances.RRULE, Instances.CALENDAR_COLOR, Instances.EVENT_COLOR};
    private static final String[] PROJECTION_4_1 = new String[]{Instances.EVENT_ID, Instances.TITLE,
            Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.EVENT_LOCATION,
            Instances.HAS_ALARM, Instances.RRULE, Instances.DISPLAY_COLOR};
    private static final int CURSOR_COLUMN_CALENDAR_COLOR = 8;
    private static final int CURSOR_COLUMN_EVENT_COLOR = 9;
    private static final int CURSOR_COLUMN_DISPLAY_COLOR = 8;
    private static final String CLOSING_BRACKET = " )";
    private static final String OR = " OR ";
    private static final String EQUALS = " = ";
    private static final String NOT_EQUALS = " != ";
    private static final String AND_BRACKET = " AND (";

    private final Context context;

    public CalendarEventProvider(Context context) {
        this.context = context;
    }

    public List<CalendarEvent> getEvents() {
        List<CalendarEvent> eventList = getTimeFilteredEventList();
        for (CalendarEvent event : getPastEventsWithCustomColor()) {
            if (!eventList.contains(event)) {
                eventList.add(event);
            }
        }
        Collections.sort(eventList);
        return eventList;
    }

    private List<CalendarEvent> getTimeFilteredEventList() {
        long millisNow = System.currentTimeMillis();
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, getStartOfTimeRange(millisNow));
        ContentUris.appendId(builder, getEndOfTimeRange(millisNow));
        Cursor cursor = context.getContentResolver().query(
                builder.build(), getProjection(), getCalendarSelection(), null, EVENT_SORT_ORDER);
        List<CalendarEvent> eventList = new ArrayList<>();
        if (cursor != null) {
            eventList = cursorToTimeFilteredEventList(cursor);
            cursor.close();
        }
        return eventList;
    }

    private long getStartOfTimeRange(long time) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return PastTime.fromValue(prefs.getString(PREF_EVENTS_START, null)).getTime(time);
    }

    private long getEndOfTimeRange(long millisNow) {
        int dateRange = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_EVENT_RANGE, PREF_EVENT_RANGE_DEFAULT));
        return dateRange > 0
                ? millisNow + DateUtils.DAY_IN_MILLIS * dateRange
                : new DateTime(millisNow).withTimeAtStartOfDay().plusDays(1).getMillis();
    }

    private String[] getProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return PROJECTION_4_1;
        }
        return PROJECTION_4_0;
    }

    private String getCalendarSelection() {
        Set<String> activeCalenders = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(CalendarPreferences.PREF_ACTIVE_CALENDARS, new HashSet<String>());
        StringBuilder stringBuilder = new StringBuilder(EVENT_SELECTION);
        if (!activeCalenders.isEmpty()) {
            stringBuilder.append(AND_BRACKET);
            Iterator<String> iterator = activeCalenders.iterator();
            while (iterator.hasNext()) {
                String calendarId = iterator.next();
                stringBuilder.append(Instances.CALENDAR_ID);
                stringBuilder.append(EQUALS);
                stringBuilder.append(calendarId);
                if (iterator.hasNext()) {
                    stringBuilder.append(OR);
                }
            }
            stringBuilder.append(CLOSING_BRACKET);
        }
        return stringBuilder.toString();
    }

    private List<CalendarEvent> cursorToTimeFilteredEventList(Cursor calendarCursor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fillAllDayEvents = prefs.getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT);
        List<CalendarEvent> eventList = new ArrayList<>();
        for (int i = 0; i < calendarCursor.getCount(); i++) {
            calendarCursor.moveToPosition(i);
            CalendarEvent event = createCalendarEvent(calendarCursor);
            setupDayOneEntry(eventList, event);
            if (!event.isAllDay() || fillAllDayEvents) {
                createFollowingEntries(eventList, event);
            }
        }
        return eventList;
    }

    private List<CalendarEvent> getPastEventsWithCustomColor() {
        List<CalendarEvent> eventList = new ArrayList<>();
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_PAST_EVENTS_WITH_COLOR, PREF_SHOW_PAST_EVENTS_WITH_COLOR_DEFAULT)) {
            Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, 0);
            ContentUris.appendId(builder, System.currentTimeMillis());
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(builder.build(), getProjection(),
                    getPastEventsWithCustomColorSelection(),
                    null, EVENT_SORT_ORDER);
            if (cursor != null) {
                eventList = cursorToPastEventsWithCustomColor(cursor);
                cursor.close();
            }
        }
        return eventList;
    }

    private String getPastEventsWithCustomColorSelection() {
        StringBuilder stringBuilder = new StringBuilder(getCalendarSelection());
        stringBuilder.append(AND_BRACKET);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            stringBuilder.append(Instances.DISPLAY_COLOR);
            stringBuilder.append(NOT_EQUALS);
            stringBuilder.append(Instances.CALENDAR_COLOR);
        } else {
            stringBuilder.append(Instances.EVENT_COLOR);
            stringBuilder.append(NOT_EQUALS);
            stringBuilder.append("0");
        }
        stringBuilder.append(CLOSING_BRACKET);
        return stringBuilder.toString();
    }

    private List<CalendarEvent> cursorToPastEventsWithCustomColor(Cursor calendarCursor) {
        List<CalendarEvent> eventList = new ArrayList<>();
        for (int i = 0; i < calendarCursor.getCount(); i++) {
            calendarCursor.moveToPosition(i);
            eventList.add(createCalendarEvent(calendarCursor));
        }
        return eventList;
    }

    private void setupDayOneEntry(List<CalendarEvent> eventList, CalendarEvent event) {
        if (isEqualOrAfterTodayAtMidnight(event.getStartDate())) {
            if (event.daysSpanned() > 1) {
                CalendarEvent clone = event.clone();
                clone.setEndDate(event.getStartDay().plusDays(1));
                clone.setSpansMultipleDays(true);
                clone.setOriginalEvent(event);
                eventList.add(clone);
            } else {
                eventList.add(event);
            }
        }
    }

    private void createFollowingEntries(List<CalendarEvent> eventList, CalendarEvent event) {
        int daysCovered = event.daysSpanned();
        for (int j = 1; j < daysCovered; j++) {
            DateTime startDate = event.getStartDay().plusDays(j);
            if (isEqualOrAfterTodayAtMidnight(startDate)) {
                DateTime endDate;
                if (j < daysCovered - 1) {
                    endDate = startDate.plusDays(1);
                } else {
                    endDate = event.getEndDate();
                }
                eventList.add(cloneAsSpanningEvent(event, startDate, endDate));
            }
        }
    }

    private boolean isEqualOrAfterTodayAtMidnight(DateTime startDate) {
        DateTime startOfDay = DateTime.now().withTimeAtStartOfDay();
        return startDate.isEqual(startOfDay) || startDate.isAfter(startOfDay);
    }

    private CalendarEvent cloneAsSpanningEvent(CalendarEvent eventEntry, DateTime startDate,
                                               DateTime endDate) {
        CalendarEvent clone = eventEntry.clone();
        clone.setStartDate(startDate);
        clone.setEndDate(endDate);
        clone.setSpansMultipleDays(true);
        clone.setOriginalEvent(eventEntry);
        return clone;
    }

    private CalendarEvent createCalendarEvent(Cursor calendarCursor) {
        CalendarEvent event = new CalendarEvent();
        event.setEventId(calendarCursor.getInt(0));
        event.setTitle(calendarCursor.getString(1));
        event.setStartDate(new DateTime(calendarCursor.getLong(2)));
        event.setEndDate(new DateTime(calendarCursor.getLong(3)));
        event.setAllDay(calendarCursor.getInt(4) > 0);
        event.setLocation(calendarCursor.getString(5));
        event.setAlarmActive(calendarCursor.getInt(6) > 0);
        event.setRecurring(calendarCursor.getString(7) != null);
        event.setColor(getAsOpaque(getEventColor(calendarCursor)));
        if (event.isAllDay()) {
            DateTime startDate = event.getStartDate();
            long converted = startDate.getZone().convertLocalToUTC(startDate.getMillis(), true);
            event.setStartDate(new DateTime(converted));
            DateTime endDate = event.getEndDate();
            converted = endDate.getZone().convertLocalToUTC(endDate.getMillis(), true);
            event.setEndDate(new DateTime(converted));
        }
        return event;
    }

    private int getEventColor(Cursor calendarCursor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return calendarCursor.getInt(CURSOR_COLUMN_DISPLAY_COLOR);
        } else {
            int eventColor = calendarCursor.getInt(CURSOR_COLUMN_EVENT_COLOR);
            if (eventColor > 0) {
                return eventColor;
            }
            return calendarCursor.getInt(CURSOR_COLUMN_CALENDAR_COLOR);
        }
    }

    private int getAsOpaque(int color) {
        return argb(255, red(color), green(color), blue(color));
    }
}
