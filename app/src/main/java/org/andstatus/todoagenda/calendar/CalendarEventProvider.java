package org.andstatus.todoagenda.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.CalendarIntentUtil;
import org.andstatus.todoagenda.util.MyClock;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import io.vavr.control.Try;

public class CalendarEventProvider extends EventProvider {
    private static final String TAG = CalendarEventProvider.class.getSimpleName();
    private static final String[] EVENT_SOURCES_PROJECTION = new String[]{CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.ACCOUNT_NAME};

    public static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC ";
    private static final String EVENT_SELECTION = Instances.SELF_ATTENDEE_STATUS + "!="
            + Attendees.ATTENDEE_STATUS_DECLINED;

    public CalendarEventProvider(EventProviderType type, Context context, int widgetId) {
        super(type, context, widgetId);
    }

    List<CalendarEvent> queryEvents() {
        initialiseParameters();
        myContentResolver.onQueryEvents();
        if (myContentResolver.isPermissionNeeded(context, type.permission) ||
                getSettings().getActiveEventSources(type).isEmpty()) {
            return Collections.emptyList();
        }
        List<CalendarEvent> eventList = getTimeFilteredEventList();
        if (getSettings().getShowPastEventsWithDefaultColor()) {
            addPastEventsWithDefaultColor(eventList);
        }

        if (getSettings().getFilterMode() != FilterMode.NO_FILTERING) {
            if (getSettings().getShowOnlyClosestInstanceOfRecurringEvent()) {
                filterShowOnlyClosestInstanceOfRecurringEvent(eventList);
            }
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
        List<CalendarEvent> toRemove = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            CalendarEvent otherEvent = eventIds.get(event.getEventId());
            if (otherEvent == null) {
                eventIds.put(event.getEventId(), event);
            } else if (Math.abs(getSettings().clock().getNumberOfMinutesTo(event.getStartDate())) <
                    Math.abs(getSettings().clock().getNumberOfMinutesTo(otherEvent.getStartDate()))) {
                toRemove.add(otherEvent);
                eventIds.put(event.getEventId(), event);
            } else {
                toRemove.add(event);
            }
        }
        eventList.removeAll(toRemove);
    }

    public DateTime getEndOfTimeRange() {
        return mEndOfTimeRange;
    }

    public DateTime getStartOfTimeRange() {
        return mStartOfTimeRange;
    }

    private List<CalendarEvent> getTimeFilteredEventList() {
        FilterMode filterMode = getSettings().getFilterMode();

        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, (filterMode == FilterMode.NORMAL_FILTER
                ? mStartOfTimeRange : MyClock.DATETIME_MIN).getMillis());
        ContentUris.appendId(builder, (filterMode == FilterMode.NORMAL_FILTER
                ? mEndOfTimeRange : MyClock.DATETIME_MAX).getMillis());
        List<CalendarEvent> eventList = queryList(builder.build(), getCalendarSelection());

        switch (filterMode) {   // TODO: Implement fully...
            case NO_FILTERING:
                break;
            default:
                // Filters in a query are not exactly correct for AllDay events: for them that filter
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
                break;
        }
        return eventList;
    }

    private String getCalendarSelection() {
        List<OrderedEventSource> activeSources = getSettings().getActiveEventSources(type);
        StringBuilder stringBuilder = new StringBuilder(EVENT_SELECTION);
        if (!activeSources.isEmpty()) {
            stringBuilder.append(AND_BRACKET);
            Iterator<OrderedEventSource> iterator = activeSources.iterator();
            while (iterator.hasNext()) {
                EventSource source = iterator.next().source;
                stringBuilder.append(Instances.CALENDAR_ID);
                stringBuilder.append(EQUALS);
                stringBuilder.append(source.getId());
                if (iterator.hasNext()) {
                    stringBuilder.append(OR);
                }
            }
            stringBuilder.append(CLOSING_BRACKET);
        }
        return stringBuilder.toString();
    }

    private List<CalendarEvent> queryList(Uri uri, String selection) {
        return myContentResolver.foldEvents(uri, getProjection(), selection, null, EVENT_SORT_ORDER,
                new ArrayList<>(), eventList -> cursor -> {
                    CalendarEvent event = createCalendarEvent(cursor);
                    if (!eventList.contains(event) && !mKeywordsFilter.matched(event.getTitle())) {
                        eventList.add(event);
                    }
                    return eventList;
                });
    }

    public static String[] getProjection() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add(Instances.CALENDAR_ID);
        columnNames.add(Instances.EVENT_ID);
        columnNames.add(Instances.TITLE);
        columnNames.add(Instances.BEGIN);
        columnNames.add(Instances.END);
        columnNames.add(Instances.ALL_DAY);
        columnNames.add(Instances.EVENT_LOCATION);
        columnNames.add(Instances.HAS_ALARM);
        columnNames.add(Instances.RRULE);
        columnNames.add(Instances.DISPLAY_COLOR);
        columnNames.add(Instances.CALENDAR_COLOR);
        return columnNames.toArray(new String[0]);
    }

    private List<CalendarEvent> getPastEventsWithColorList() {
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, 0);
        ContentUris.appendId(builder, getSettings().clock().now().getMillis());
        return queryList(builder.build(), getPastEventsWithColorSelection()).stream()
            .filter(ev -> getSettings().getFilterMode() != FilterMode.DEBUG_FILTER || ev.hasDefaultCalendarColor())
            .collect(Collectors.toList());
    }

    private String getPastEventsWithColorSelection() {
        return getCalendarSelection() +
            AND_BRACKET +
                Instances.DISPLAY_COLOR + EQUALS + Instances.CALENDAR_COLOR +
            CLOSING_BRACKET;
    }

    private CalendarEvent createCalendarEvent(Cursor cursor) {
        OrderedEventSource source = getSettings()
            .getActiveEventSource(type, cursor.getInt(cursor.getColumnIndex(Instances.CALENDAR_ID)));

        boolean allDay = cursor.getInt(cursor.getColumnIndex(Instances.ALL_DAY)) > 0;
        CalendarEvent event = new CalendarEvent(getSettings(), context, widgetId, allDay);
        event.setEventSource(source);
        event.setEventId(cursor.getInt(cursor.getColumnIndex(Instances.EVENT_ID)));
        event.setTitle(cursor.getString(cursor.getColumnIndex(Instances.TITLE)));
        event.setStartMillis(cursor.getLong(cursor.getColumnIndex(Instances.BEGIN)));
        event.setEndMillis(cursor.getLong(cursor.getColumnIndex(Instances.END)));
        event.setLocation(cursor.getString(cursor.getColumnIndex(Instances.EVENT_LOCATION)));
        event.setAlarmActive(cursor.getInt(cursor.getColumnIndex(Instances.HAS_ALARM)) > 0);
        event.setRecurring(cursor.getString(cursor.getColumnIndex(Instances.RRULE)) != null);
        event.setColor(getAsOpaque(cursor.getInt(cursor.getColumnIndex(Instances.DISPLAY_COLOR))));
        getColumnIndex(cursor, Instances.CALENDAR_COLOR)
                .map(ind -> getAsOpaque(cursor.getInt(ind)))
                .ifPresent(event::setCalendarColor);

        return event;
    }

    @Override
    public Try<List<EventSource>> fetchAvailableSources() {
        return myContentResolver.foldAvailableSources(
                CalendarContract.Calendars.CONTENT_URI.buildUpon().build(),
                EVENT_SOURCES_PROJECTION,
                new ArrayList<>(),
                eventSources -> cursor -> {
                    int indId = cursor.getColumnIndex(CalendarContract.Calendars._ID);
                    int indTitle = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
                    int indSummary = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME);
                    int indColor = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR);
                    EventSource source = new EventSource(type, cursor.getInt(indId), cursor.getString(indTitle),
                            cursor.getString(indSummary), cursor.getInt(indColor), true);
                    eventSources.add(source);
                    return eventSources;
                });
    }

    public Intent createViewEventIntent(CalendarEvent event) {
        Intent intent = CalendarIntentUtil.createViewIntent();
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getEventId()));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEndMillis());
        return intent;
    }

}
