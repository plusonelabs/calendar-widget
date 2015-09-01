package com.plusonelabs.calendar.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.util.Log;

import com.plusonelabs.calendar.BuildConfig;
import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
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
    private DateTime mStartOfTimeRange;
    private DateTime mEndOfTimeRange;

    public CalendarEventProvider(Context context) {
        this.context = context;
    }

    public List<CalendarEvent> getEvents() {
        initialiseParameters();
        List<CalendarEvent> eventList = getTimeFilteredEventList();
        for (CalendarEvent event : getPastEventWithColorList()) {
            if (eventList.contains(event)) {
                eventList.remove(event);
            }
            eventList.add(event);
        }
        return eventList;
    }

    private void initialiseParameters() {
        mKeywordsFilter = new KeywordsFilter(CalendarPreferences.getHideBasedOnKeywords(context));
        // TODO: These values are not exactly correct for AllDay events: for them the filter time should be moved by a time zone... (i.e. by several hours)
        mStartOfTimeRange = CalendarPreferences.getEventsEnded(context)
                .endedAt(DateUtil.now());
        mEndOfTimeRange = getEndOfTimeRange(DateUtil.now());
    }

    public DateTime getEndOfTimeRange() {
        return mEndOfTimeRange;
    }

    public DateTime getStartOfTimeRange() {
        return mStartOfTimeRange;
    }

    private DateTime getEndOfTimeRange(DateTime now) {
        int dateRange = CalendarPreferences.getEventRange(context);
        return dateRange > 0
                ? now.plusDays(dateRange)
                : now.withTimeAtStartOfDay().plusDays(1);
    }

    private List<CalendarEvent> getTimeFilteredEventList() {
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mStartOfTimeRange.getMillis());
        ContentUris.appendId(builder, mEndOfTimeRange.getMillis());
        return queryList(builder.build(), getCalendarSelection());
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
            for (CalendarEvent event : eventList) {
                event.setDefaultCalendarColor();
            }
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
            fixAllDayEvent(event);
        }
        return event;
    }

    private void fixAllDayEvent(CalendarEvent event) {
        event.setStartDate(fixTimeOfAllDayEvent(event.getStartDate()));
        event.setEndDate(fixTimeOfAllDayEvent(event.getEndDate()));
        if (!event.getEndDate().isAfter(event.getStartDate())) {
            event.setEndDate(event.getStartDate().plusDays(1));
        }
    }

    private DateTime fixTimeOfAllDayEvent(DateTime date) {
        String msgLog = "";
        DateTime fixed;
        try {
            DateTimeZone zone = date.getZone();
            msgLog += "date=" + date + " ( " + zone + ")";
            DateTime utcDate = date.toDateTime(DateTimeZone.UTC);
            LocalDateTime ldt = new LocalDateTime()
                    .withYear(utcDate.getYear())
                    .withMonthOfYear(utcDate.getMonthOfYear())
                    .withDayOfMonth(utcDate.getDayOfMonth())
                    .withMillisOfDay(0);
            int hour = 0;
            while (zone.isLocalDateTimeGap(ldt)) {
                Log.v("fixTimeOfAllDayEvent", "Local Date Time Gap: " + ldt + "; " + msgLog);
                ldt = ldt.withHourOfDay(++hour);
            }
            fixed = ldt.toDateTime();
            msgLog += " -> " + fixed;
            if (BuildConfig.DEBUG) {
                Log.v("fixTimeOfAllDayEvent", msgLog);
            }
        } catch (org.joda.time.IllegalInstantException e) {
            throw new org.joda.time.IllegalInstantException(msgLog + " caused by: " + e);
        }
        return fixed;
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
