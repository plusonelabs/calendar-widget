package com.plusonelabs.calendar.calendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.IEventVisualizer;
import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.prefs.InstanceSettings;
import com.plusonelabs.calendar.widget.CalendarEntry;
import com.plusonelabs.calendar.widget.EventEntryLayout;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.plusonelabs.calendar.RemoteViewsUtil.setAlpha;
import static com.plusonelabs.calendar.RemoteViewsUtil.setBackgroundColor;
import static com.plusonelabs.calendar.RemoteViewsUtil.setImageFromAttr;
import static com.plusonelabs.calendar.Theme.themeNameToResId;

public class CalendarEventVisualizer implements IEventVisualizer<CalendarEntry> {

    private final Context context;
    private final int widgetId;
    private final CalendarEventProvider calendarContentProvider;

    public CalendarEventVisualizer(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        calendarContentProvider = new CalendarEventProvider(context, widgetId);
    }

    public RemoteViews getRemoteView(WidgetEntry eventEntry) {
        CalendarEntry entry = (CalendarEntry) eventEntry;
        EventEntryLayout eventEntryLayout = getSettings().getEventEntryLayout();
        RemoteViews rv = new RemoteViews(context.getPackageName(), eventEntryLayout.layoutId);
        rv.setOnClickFillInIntent(R.id.event_entry, entry.getEvent().createOpenCalendarEventIntent());
        eventEntryLayout.visualizeEvent(entry, rv);
        setAlarmActive(entry, rv);
        setRecurring(entry, rv);
        setColor(entry, rv);
        return rv;
    }

    private void setAlarmActive(CalendarEntry entry, RemoteViews rv) {
        boolean showIndication = entry.isAlarmActive() && getSettings().getIndicateAlerts();
        setIndicator(rv, showIndication, R.id.event_entry_indicator_alarm, R.attr.eventEntryAlarm);
    }

    private void setRecurring(CalendarEntry entry, RemoteViews rv) {
        boolean showIndication = entry.isRecurring() && getSettings().getIndicateRecurring();
        setIndicator(rv, showIndication, R.id.event_entry_indicator_recurring, R.attr.eventEntryRecurring);
    }

    private void setIndicator(RemoteViews rv, boolean showIndication, int viewId, int imageAttrId) {
        if (showIndication) {
            rv.setViewVisibility(viewId, View.VISIBLE);
            setImageFromAttr(context, rv, viewId, imageAttrId);
            int themeId = themeNameToResId(getSettings().getEntryTheme());
            int alpha = 255;
            if (themeId == R.style.Theme_Calendar_Dark || themeId == R.style.Theme_Calendar_Light) {
                alpha = 128;
            }
            setAlpha(rv, viewId, alpha);
        } else {
            rv.setViewVisibility(viewId, View.GONE);
        }
    }

    private void setColor(CalendarEntry entry, RemoteViews rv) {
        setBackgroundColor(rv, R.id.event_entry_color, entry.getColor());
        if (entry.getEndDate().isBefore(DateUtil.now(entry.getEndDate().getZone()))) {
            setBackgroundColor(rv, R.id.event_entry, getSettings().getPastEventsBackgroundColor());
        } else {
            setBackgroundColor(rv, R.id.event_entry, 0);
        }
    }

    @NonNull
    private InstanceSettings getSettings() {
        return InstanceSettings.fromId(context, widgetId);
    }

    public int getViewTypeCount() {
        return 1;
    }

    public List<CalendarEntry> getEventEntries() {
        List<CalendarEntry> entries = createEntryList(calendarContentProvider.getEvents());
        Collections.sort(entries);
        return entries;
    }

    private List<CalendarEntry> createEntryList(List<CalendarEvent> eventList) {
        boolean fillAllDayEvents = getSettings().getFillAllDayEvents();
        List<CalendarEntry> entryList = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            CalendarEntry dayOneEntry = setupDayOneEntry(entryList, event);
            if (fillAllDayEvents) {
                createFollowingEntries(entryList, dayOneEntry);
            }
        }
        return entryList;
    }

    private CalendarEntry setupDayOneEntry(List<CalendarEntry> entryList, CalendarEvent event) {
        CalendarEntry dayOneEntry = CalendarEntry.fromEvent(event);
        DateTime firstDate = event.getStartDate();
        DateTime dayOfStartOfTimeRange = calendarContentProvider.getStartOfTimeRange()
                .withTimeAtStartOfDay();
        if (!event.hasDefaultCalendarColor()
                && firstDate.isBefore(calendarContentProvider.getStartOfTimeRange())
                && event.getEndDate().isAfter(calendarContentProvider.getStartOfTimeRange())) {
            if (event.isAllDay() || firstDate.isBefore(dayOfStartOfTimeRange)) {
                firstDate = dayOfStartOfTimeRange;
            }
        }
        DateTime today = DateUtil.now(event.getStartDate().getZone()).withTimeAtStartOfDay();
        if (event.isActive() && firstDate.isBefore(today)) {
            firstDate = today;
        }
        dayOneEntry.setStartDate(firstDate);
        DateTime nextDay = dayOneEntry.getStartDay().plusDays(1);
        if (event.getEndDate().isAfter(nextDay)) {
            dayOneEntry.setEndDate(nextDay);
        }
        entryList.add(dayOneEntry);
        return dayOneEntry;
    }

    private void createFollowingEntries(List<CalendarEntry> entryList, CalendarEntry dayOneEntry) {
        DateTime endDate = dayOneEntry.getEvent().getEndDate();
        if (endDate.isAfter(calendarContentProvider.getEndOfTimeRange())) {
            endDate = calendarContentProvider.getEndOfTimeRange();
        }
        DateTime thisDay = dayOneEntry.getStartDay().plusDays(1).withTimeAtStartOfDay();
        while (thisDay.isBefore(endDate)) {
            DateTime nextDay = thisDay.plusDays(1);
            CalendarEntry nextEntry = CalendarEntry.fromEvent(dayOneEntry.getEvent());
            nextEntry.setStartDate(thisDay);
            if (endDate.isAfter(nextDay)) {
                nextEntry.setEndDate(nextDay);
            } else {
                nextEntry.setEndDate(endDate);
            }
            entryList.add(nextEntry);
            thisDay = nextDay;
        }
    }

    public Class<? extends CalendarEntry> getSupportedEventEntryType() {
        return CalendarEntry.class;
    }

}
