package com.plusonelabs.calendar.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.prefs.InstanceSettings;
import com.plusonelabs.calendar.util.PermissionsUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
    private static final String CLOSING_BRACKET = " )";
    private static final String OR = " OR ";
    private static final String EQUALS = " = ";
    private static final String AND_BRACKET = " AND (";

    private final Context context;
    private final int widgetId;

    // Below are parameters, which may change in settings
    private DateTimeZone zone;
    private KeywordsFilter mKeywordsFilter;
    private DateTime mStartOfTimeRange;
    private DateTime mEndOfTimeRange;

    public CalendarEventProvider(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
    }

    public List<CalendarEvent> getEvents() {
        initialiseParameters();
        if (!PermissionsUtil.arePermissionsGranted(context)) {
            return new ArrayList<>();
        }
        List<CalendarEvent> eventList = getTimeFilteredEventList();
        if (getSettings().getShowPastEventsWithDefaultColor()) {
            addPastEventsWithDefaultColor(eventList);
        }
        if (getSettings().getShowOnlyClosestInstanceOfRecurringEvent()) {
            filterShowOnlyClosestInstanceOfRecurringEvent(eventList);
        }
        return eventList;
    }

    private void addPastEventsWithDefaultColor(List<CalendarEvent> eventList) {
        for (CalendarEvent event : getPastEventsWithColorList()) {
            if (eventList.contains(event)) {
                eventList.remove(event);
            }
            eventList.add(event);
        }
    }

    private void filterShowOnlyClosestInstanceOfRecurringEvent(@NonNull List<CalendarEvent> eventList) {
        SparseArray<CalendarEvent> eventIds = new SparseArray<>();
        List<CalendarEvent> toDelete = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            CalendarEvent otherEvent = eventIds.get(event.getEventId());
            if (otherEvent == null) {
                eventIds.put(event.getEventId(), event);
            } else if (Math.abs(event.getStartDate().getMillis() -
                    DateUtil.now(zone).getMillis()) <
                    Math.abs(otherEvent.getStartDate().getMillis() -
                            DateUtil.now(zone).getMillis())) {
                toDelete.add(otherEvent);
                eventIds.put(event.getEventId(), event);
            } else {
                toDelete.add(event);
            }
        }
        eventList.removeAll(toDelete);
    }

    private void initialiseParameters() {
        zone = getSettings().getTimeZone();
        mKeywordsFilter = new KeywordsFilter(getSettings().getHideBasedOnKeywords());
        mStartOfTimeRange = getSettings().getEventsEnded().endedAt(DateUtil.now(zone));
        mEndOfTimeRange = getEndOfTimeRange(DateUtil.now(zone));
    }

    public DateTime getEndOfTimeRange() {
        return mEndOfTimeRange;
    }

    public DateTime getStartOfTimeRange() {
        return mStartOfTimeRange;
    }

    private DateTime getEndOfTimeRange(DateTime now) {
        int dateRange = getSettings().getEventRange();
        return dateRange > 0
                ? now.plusDays(dateRange)
                : now.withTimeAtStartOfDay().plusDays(1);
    }

    @NonNull
    private InstanceSettings getSettings() {
        return InstanceSettings.fromId(context, widgetId);
    }

    private List<CalendarEvent> getTimeFilteredEventList() {
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mStartOfTimeRange.getMillis());
        ContentUris.appendId(builder, mEndOfTimeRange.getMillis());
        List<CalendarEvent> eventList = queryList(builder.build(), getCalendarSelection());
        // Above filters are not exactly correct for AllDay events: for them that filter
        // time should be moved by a time zone... (i.e. by several hours)
        // This is why we need to do additional filtering after querying a Content Provider:
        for (Iterator<CalendarEvent> it = eventList.iterator(); it.hasNext(); ) {
            CalendarEvent event = it.next();
            if (!event.getEndDate().isAfter(mStartOfTimeRange)
                    || !mEndOfTimeRange.isAfter(event.getStartDate())) {
                // We remove using Iterator to avoid ConcurrentModificationException
                it.remove();
            }
        }
        return eventList;
    }

    private String getCalendarSelection() {
        Set<String> activeCalendars = getSettings().getActiveCalendars();
        StringBuilder stringBuilder = new StringBuilder(EVENT_SELECTION);
        if (!activeCalendars.isEmpty()) {
            stringBuilder.append(AND_BRACKET);
            Iterator<String> iterator = activeCalendars.iterator();
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
        CalendarQueryResult result = new CalendarQueryResult(getSettings(), uri, getProjection(),
                selection, null, EVENT_SORT_ORDER);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, getProjection(),
                    selection, null, EVENT_SORT_ORDER);
            if (cursor != null) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    if (CalendarQueryResultsStorage.getNeedToStoreResults()) {
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
        CalendarQueryResultsStorage.store(result);
        return eventList;
    }

    public static String[] getProjection() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add(Instances.EVENT_ID);
        columnNames.add(Instances.TITLE);
        columnNames.add(Instances.BEGIN);
        columnNames.add(Instances.END);
        columnNames.add(Instances.ALL_DAY);
        columnNames.add(Instances.EVENT_LOCATION);
        columnNames.add(Instances.HAS_ALARM);
        columnNames.add(Instances.RRULE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            columnNames.add(Instances.DISPLAY_COLOR);
        } else {
            columnNames.add(Instances.CALENDAR_COLOR);
            columnNames.add(Instances.EVENT_COLOR);
        }
        return columnNames.toArray(new String[columnNames.size()]);
    }

    private List<CalendarEvent> getPastEventsWithColorList() {
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, 0);
        ContentUris.appendId(builder, DateUtil.now(zone).getMillis());
        List<CalendarEvent> eventList = queryList(builder.build(), getPastEventsWithColorSelection());
        for (CalendarEvent event : eventList) {
            event.setDefaultCalendarColor();
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

    private CalendarEvent createCalendarEvent(Cursor cursor) {
        boolean allDay = cursor.getInt(cursor.getColumnIndex(Instances.ALL_DAY)) > 0;
        CalendarEvent event = new CalendarEvent(context, widgetId, zone, allDay);
        event.setEventId(cursor.getInt(cursor.getColumnIndex(Instances.EVENT_ID)));
        event.setTitle(cursor.getString(cursor.getColumnIndex(Instances.TITLE)));
        event.setStartMillis(cursor.getLong(cursor.getColumnIndex(Instances.BEGIN)));
        event.setEndMillis(cursor.getLong(cursor.getColumnIndex(Instances.END)));
        event.setLocation(cursor.getString(cursor.getColumnIndex(Instances.EVENT_LOCATION)));
        event.setAlarmActive(cursor.getInt(cursor.getColumnIndex(Instances.HAS_ALARM)) > 0);
        event.setRecurring(cursor.getString(cursor.getColumnIndex(Instances.RRULE)) != null);
        event.setColor(getAsOpaque(getEventColor(cursor)));
        return event;
    }

    private int getEventColor(Cursor cursor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return cursor.getInt(cursor.getColumnIndex(Instances.DISPLAY_COLOR));
        } else {
            int eventColor = cursor.getInt(cursor.getColumnIndex(Instances.EVENT_COLOR));
            if (eventColor > 0) {
                return eventColor;
            }
            return cursor.getInt(cursor.getColumnIndex(Instances.CALENDAR_COLOR));
        }
    }

    private int getAsOpaque(int color) {
        return argb(255, red(color), green(color), blue(color));
    }
}
