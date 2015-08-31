package com.plusonelabs.calendar.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.EndedSometimeAgo;
import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class CalendarEventProvider {

    public static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC ";
    private static final String EVENT_SELECTION = Instances.SELF_ATTENDEE_STATUS + "!="
            + Attendees.ATTENDEE_STATUS_DECLINED;
    private static final String[] PROJECTION_4_0 = new String[]{Instances.EVENT_ID, Instances.TITLE,
            Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.EVENT_LOCATION,
            Instances.HAS_ALARM, Instances.RRULE, Instances.CALENDAR_COLOR, Instances.EVENT_COLOR};
    private static final int CURSOR_COLUMN_CALENDAR_COLOR = 8;
    private static final int CURSOR_COLUMN_EVENT_COLOR = 9;
    private static final String[] PROJECTION_4_1 = new String[]{Instances.EVENT_ID, Instances.TITLE,
            Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.EVENT_LOCATION,
            Instances.HAS_ALARM, Instances.RRULE, Instances.DISPLAY_COLOR};
    private static final int CURSOR_COLUMN_DISPLAY_COLOR = 8;
    private static final String CLOSING_BRACKET = " )";
    private static final String OR = " OR ";
    private static final String EQUALS = " = ";
    private static final String AND_BRACKET = " AND (";

    private final Context context;
    private KeywordsFilter mKeywordsFilter;
    private boolean mFillAllDayEvents;

    public CalendarEventProvider(Context context) {
        this.context = context;
    }

    public List<CalendarEvent> getEvents() {
        initialiseParameters();
        List<CalendarEvent> eventList = getTimeFilteredEventList();
        for (CalendarEvent event : getPastEventWithColorList()) {
            if (!eventList.contains(event)) {
                eventList.add(event);
            }
        }
        eventList = expandEventList(eventList);
        Collections.sort(eventList);
        return eventList;
    }

    private void initialiseParameters() {
        mKeywordsFilter = new KeywordsFilter(CalendarPreferences.getHideBasedOnKeywords(context));
        mFillAllDayEvents = CalendarPreferences.getFillAllDayEvents(context);
    }

    private List<CalendarEvent> getTimeFilteredEventList() {
        long millisNow = DateUtil.now().getMillis();
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, getStartOfTimeRange(millisNow));
        ContentUris.appendId(builder, getEndOfTimeRange(millisNow));
        return queryList(builder.build(), getCalendarSelection());
    }

    private long getStartOfTimeRange(long millisNow) {
        return EndedSometimeAgo.fromValue(CalendarPreferences.getEventsEnded(context))
                .endedAt(millisNow);
    }

    private long getEndOfTimeRange(long millisNow) {
        int dateRange = CalendarPreferences.getEventRange(context);
        return dateRange > 0
                ? millisNow + DateUtils.DAY_IN_MILLIS * dateRange
                : new DateTime(millisNow).withTimeAtStartOfDay().plusDays(1).getMillis();
    }

    private String getCalendarSelection() {
        Set<String> activeCalenders = CalendarPreferences.getActiveCalendars(context);
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

    private List<CalendarEvent> queryList(Uri uri, String selection) {
        List<CalendarEvent> eventList = new ArrayList<>();
        CalendarQueryResult result = new CalendarQueryResult(uri, getProjection(), selection, null, EVENT_SORT_ORDER);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, getProjection(),
                    selection, null, EVENT_SORT_ORDER);
            if (cursor != null) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    if (CalendarQueryStoredResults.getNeedToStoreResults()) {
                        result.addRow(cursor);
                    }
                    CalendarEvent event = createCalendarEvent(cursor);
                    if (!eventList.contains(event) && !mKeywordsFilter.matched(event.getTitle())) {
                        eventList.add(event);
                    }
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        CalendarQueryStoredResults.store(result);
        return eventList;
    }

    public static String[] getProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return PROJECTION_4_1;
        }
        return PROJECTION_4_0;
    }

    private List<CalendarEvent> getPastEventWithColorList() {
        List<CalendarEvent> eventList = new ArrayList<>();
        if (CalendarPreferences.getShowPastEventsWithDefaultColor(context)) {
            Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, 0);
            ContentUris.appendId(builder, DateUtil.now().getMillis());
            eventList = queryList(builder.build(), getPastEventsWithColorSelection());
        }
        return eventList;
    }

    private String getPastEventsWithColorSelection() {
        StringBuilder stringBuilder = new StringBuilder(getCalendarSelection());
        stringBuilder.append(AND_BRACKET);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            stringBuilder.append(Instances.DISPLAY_COLOR);
            stringBuilder.append(EQUALS);
            stringBuilder.append(Instances.CALENDAR_COLOR);
        } else {
            stringBuilder.append(Instances.EVENT_COLOR);
            stringBuilder.append(EQUALS);
            stringBuilder.append("0");
        }
        stringBuilder.append(CLOSING_BRACKET);
        return stringBuilder.toString();
    }

    private List<CalendarEvent> expandEventList(List<CalendarEvent> eventList) {
        List<CalendarEvent> expandedList = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            setupDayOneEntry(expandedList, event);
            if (!event.isAllDay() || mFillAllDayEvents) {
                createFollowingEntries(expandedList, event);
            }
        }
        return expandedList;
    }

    private void setupDayOneEntry(List<CalendarEvent> eventList, CalendarEvent event) {
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
        DateTime startOfDay = DateUtil.now().withTimeAtStartOfDay();
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
