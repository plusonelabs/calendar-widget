package org.andstatus.todoagenda;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.DayHeaderVisualizer;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarEventPendingIntent;

public class EventRemoteViewsFactory implements RemoteViewsFactory {

    private final Context context;
    private final int widgetId;
    private volatile List<? extends WidgetEntry> widgetEntries = new ArrayList<>();
    private final List<WidgetEntryVisualizer<? extends WidgetEntry>> visualizers = new ArrayList<>();

    public EventRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
    }

    public void onCreate() {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(getSettings()));
    }

    public void onDestroy() {
        // Empty
    }

    public int getCount() {
        return widgetEntries.size();
    }

    public RemoteViews getViewAt(int position) {
        if (position < widgetEntries.size()) {
            WidgetEntry entry = widgetEntries.get(position);
            for (WidgetEntryVisualizer<? extends WidgetEntry> visualizer : visualizers) {
                RemoteViews views = visualizer.getRemoteView(entry);
                if (views != null) return views;
            }
        }
        return null;
    }

    @NonNull
    private InstanceSettings getSettings() {
        return AllSettings.instanceFromId(context, widgetId);
    }

    public void onDataSetChanged() {
        visualizers.clear();
        visualizers.add(new DayHeaderVisualizer(getSettings().getDayHeaderThemeContext(), widgetId));
        for (EventProviderType type : EventProviderType.values()) {
            if (type.hasEventSources()) {
                visualizers.add(type.getVisualizer(context, widgetId));
            }
        }

        if (getSettings().getShowDayHeaders())
            widgetEntries = addDayHeaders(getEventEntries());
        else
            widgetEntries = getEventEntries();
    }

    private List<WidgetEntry> getEventEntries() {
        List<WidgetEntry> entries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> visualizer : visualizers) {
            entries.addAll(visualizer.getEventEntries());
        }
        Collections.sort(entries);
        return entries;
    }

    private List<WidgetEntry> addDayHeaders(List<WidgetEntry> listIn) {
        List<WidgetEntry> listOut = new ArrayList<>();
        if (!listIn.isEmpty()) {
            InstanceSettings settings = getSettings();
            DateTime today = DateUtil.now(getSettings().getTimeZone()).withTimeAtStartOfDay();
            DayHeader curDayBucket = new DayHeader(DateUtil.DATETIME_MIN);
            boolean pastEventsHeaderAdded = false;
            for (WidgetEntry entry : listIn) {
                DateTime nextStartOfDay = entry.getStartDay();
                if (settings.getShowPastEventsUnderOneHeader() && nextStartOfDay.isBefore(today)) {
                    if(!pastEventsHeaderAdded) {
                        listOut.add(curDayBucket);
                        pastEventsHeaderAdded = true;
                    }
                } else if (!nextStartOfDay.isEqual(curDayBucket.getStartDay())) {
                    if (settings.getShowDaysWithoutEvents()) {
                        addEmptyDayHeadersBetweenTwoDays(listOut, curDayBucket.getStartDay(), nextStartOfDay);
                    }
                    curDayBucket = new DayHeader(nextStartOfDay);
                    listOut.add(curDayBucket);
                }
                listOut.add(entry);
            }
        }
        return listOut;
    }

    public void logWidgetEntries(String tag) {
        for (int ind = 0; ind < getWidgetEntries().size(); ind++) {
            WidgetEntry widgetEntry = getWidgetEntries().get(ind);
            Log.v(tag, String.format("%02d ", ind) + widgetEntry.toString());
        }
    }

    List<? extends WidgetEntry> getWidgetEntries() {
        return widgetEntries;
    }

    private void addEmptyDayHeadersBetweenTwoDays(List<WidgetEntry> entries, DateTime fromDayExclusive, DateTime toDayExclusive) {
        DateTime emptyDay = fromDayExclusive.plusDays(1);
        DateTime today = DateUtil.now(getSettings().getTimeZone()).withTimeAtStartOfDay();
        if (emptyDay.isBefore(today)) {
            emptyDay = today;
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            entries.add(new DayHeader(emptyDay));
            emptyDay = emptyDay.plusDays(1);
        }
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        int result = 0;
        for (WidgetEntryVisualizer<?> visualizer : visualizers) {
            result += visualizer.getViewTypeCount();
        }
        return result;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

}
