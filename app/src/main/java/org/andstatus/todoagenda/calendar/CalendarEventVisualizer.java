package org.andstatus.todoagenda.calendar;

import android.view.View;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.AlarmIndicatorScaled;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.RecurringIndicatorScaled;
import org.andstatus.todoagenda.TextShading;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static org.andstatus.todoagenda.util.RemoteViewsUtil.setAlpha;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setImageFromAttr;

public class CalendarEventVisualizer extends WidgetEntryVisualizer<CalendarEntry> {
    private final CalendarEventProvider eventProvider;

    public CalendarEventVisualizer(EventProvider eventProvider) {
        super(eventProvider);
        this.eventProvider = (CalendarEventProvider) eventProvider;
    }

    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        if (!(eventEntry instanceof CalendarEntry)) return null;

        RemoteViews rv = super.getRemoteViews(eventEntry, position);
        CalendarEntry entry = (CalendarEntry) eventEntry;
        rv.setOnClickFillInIntent(R.id.event_entry, eventProvider.createViewEventIntent(entry.getEvent()));
        setIcon(entry, rv);
        return rv;
    }

    @Override
    protected void setIndicators(WidgetEntry eventEntry, RemoteViews rv) {
        CalendarEntry entry = (CalendarEntry) eventEntry;
        setAlarmActive(entry, rv);
        setRecurring(entry, rv);
    }

    private void setAlarmActive(CalendarEntry entry, RemoteViews rv) {
        boolean showIndicator = entry.isAlarmActive() && getSettings().getIndicateAlerts();
        for (AlarmIndicatorScaled indicator : AlarmIndicatorScaled.values()) {
            setIndicator(entry, rv,
                    showIndicator && indicator == getSettings().getTextSizeScale().alarmIndicator,
                    indicator.indicatorResId, R.attr.eventEntryAlarm);
        }
    }

    private void setRecurring(CalendarEntry entry, RemoteViews rv) {
        boolean showIndicator = entry.isRecurring() && getSettings().getIndicateRecurring();
        for (RecurringIndicatorScaled indicator : RecurringIndicatorScaled.values()) {
            setIndicator(entry, rv,
                    showIndicator && indicator == getSettings().getTextSizeScale().recurringIndicator,
                    indicator.indicatorResId, R.attr.eventEntryRecurring);
        }
    }

    private void setIndicator(CalendarEntry entry, RemoteViews rv, boolean showIndication, int viewId, int imageAttrId) {
        if (showIndication) {
            rv.setViewVisibility(viewId, View.VISIBLE);
            TextShadingPref pref = TextShadingPref.forTitle(entry);
            setImageFromAttr(getSettings().getShadingContext(pref), rv, viewId, imageAttrId);
            TextShading textShading = getSettings().getShading(pref);
            int alpha = 255;
            if (textShading == TextShading.DARK || textShading == TextShading.LIGHT) {
                alpha = 128;
            }
            setAlpha(rv, viewId, alpha);
        } else {
            rv.setViewVisibility(viewId, View.GONE);
        }
    }

    private void setIcon(CalendarEntry entry, RemoteViews rv) {
        if (getSettings().getShowEventIcon()) {
            rv.setViewVisibility(R.id.event_entry_color, View.VISIBLE);
            setBackgroundColor(rv, R.id.event_entry_color, entry.getColor());
        } else {
            rv.setViewVisibility(R.id.event_entry_color, View.GONE);
        }
        rv.setViewVisibility(R.id.event_entry_icon, View.GONE);
    }

    @Override
    public List<CalendarEntry> queryEventEntries() {
        return createEntryList(eventProvider.queryEvents());
    }

    private List<CalendarEntry> createEntryList(List<CalendarEvent> eventList) {
        boolean fillAllDayEvents = getSettings().getFillAllDayEvents();
        List<CalendarEntry> entryList = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            CalendarEntry dayOneEntry = getDayOneEntry(event);
            entryList.add(dayOneEntry);
            if (fillAllDayEvents) {
                createFollowingEntries(entryList, dayOneEntry);
            }
        }
        return entryList;
    }

    private CalendarEntry getDayOneEntry(CalendarEvent event) {
        DateTime firstDate = event.getStartDate();
        DateTime dayOfStartOfTimeRange = eventProvider.getStartOfTimeRange()
                .withTimeAtStartOfDay();
        if (!event.hasDefaultCalendarColor()   // ??? TODO: fix logic
                && firstDate.isBefore(eventProvider.getStartOfTimeRange())
                && event.getEndDate().isAfter(eventProvider.getStartOfTimeRange())) {
            if (event.isAllDay() || firstDate.isBefore(dayOfStartOfTimeRange)) {
                firstDate = dayOfStartOfTimeRange;
            }
        }
        DateTime today = getSettings().clock().now(event.getStartDate().getZone()).withTimeAtStartOfDay();
        if (event.isActive() && firstDate.isBefore(today)) {
            firstDate = today;
        }
        return CalendarEntry.fromEvent(getSettings(), event, firstDate);
    }

    private void createFollowingEntries(List<CalendarEntry> entryList, CalendarEntry dayOneEntry) {
        DateTime endDate = dayOneEntry.getEvent().getEndDate();
        if (endDate.isAfter(eventProvider.getEndOfTimeRange())) {
            endDate = eventProvider.getEndOfTimeRange();
        }
        DateTime thisDay = dayOneEntry.entryDay.plusDays(1).withTimeAtStartOfDay();
        while (thisDay.isBefore(endDate)) {
            CalendarEntry nextEntry = CalendarEntry.fromEvent(getSettings(), dayOneEntry.getEvent(), thisDay);
            entryList.add(nextEntry);
            thisDay = thisDay.plusDays(1);
        }
    }

}
