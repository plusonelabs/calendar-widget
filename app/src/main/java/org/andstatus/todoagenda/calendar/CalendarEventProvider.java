package org.andstatus.todoagenda.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.CalendarIntentUtil;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CalendarEventProvider extends EventProvider {
    private static final String[] EVENT_SOURCES_PROJECTION = new String[]{CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.ACCOUNT_NAME};

    public static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC ";
    private static final String EVENT_SELECTION = Instances.SELF_ATTENDEE_STATUS + "!="
            + Attendees.ATTENDEE_STATUS_DECLINED;

    public CalendarEventProvider(EventProviderType type, Context context, int widgetId) {
        super(type, context, widgetId);
    }

    public List<CalendarEvent> getEvents() {
        initialiseParameters();
        if (PermissionsUtil.isPermissionNeeded(context, type.permission)) {
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

    public DateTime getEndOfTimeRange() {
        return mEndOfTimeRange;
    }

    public DateTime getStartOfTimeRange() {
        return mStartOfTimeRange;
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

    @Override
    public Collection<EventSource> fetchAvailableSources() {
        List<EventSource> eventSources = new ArrayList<>();
        Uri.Builder builder = CalendarContract.Calendars.CONTENT_URI.buildUpon();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(builder.build(), EVENT_SOURCES_PROJECTION, null, null, null);
            if (cursor == null) {
                return eventSources;
            }

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                EventSource source = new EventSource(type, cursor.getInt(0), cursor.getString(1),
                        cursor.getString(3), cursor.getInt(2));
                eventSources.add(source);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return eventSources;
    }

    public Intent createViewEventIntent(CalendarEvent event) {
        Intent intent = CalendarIntentUtil.createViewIntent();
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getEventId()));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEndMillis());
        return intent;
    }

}
