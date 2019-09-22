package org.andstatus.todoagenda;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.DayHeaderVisualizer;
import org.andstatus.todoagenda.widget.LastEntry;
import org.andstatus.todoagenda.widget.LastEntryVisualizer;
import org.andstatus.todoagenda.widget.TimeSection;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final int MIN_MILLIS_BETWEEN_RELOADS = 500;
    private final Context context;
    private final int widgetId;
    private volatile List<WidgetEntry> widgetEntries = new ArrayList<>();
    private volatile List<WidgetEntryVisualizer<? extends WidgetEntry>> visualizers = new ArrayList<>();
    private volatile long prevReloadFinishedAt = 0;

    public RemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        visualizers.add(new LastEntryVisualizer(context, widgetId));
        widgetEntries.add(new LastEntry(LastEntry.LastEntryType.NOT_LOADED, DateUtil.now(getSettings().getTimeZone())));
        logEvent("Init");
    }

    private void logEvent(String message) {
        Log.d(this.getClass().getSimpleName(), widgetId + " " + message);
    }

    public void onCreate() {
        logEvent("onCreate");
        reload();
    }

    public void onDestroy() {
        logEvent("onDestroy");
    }

    public int getCount() {
        logEvent("getCount:" + widgetEntries.size());
        return widgetEntries.size();
    }

    public RemoteViews getViewAt(int position) {
        if (position < widgetEntries.size()) {
            WidgetEntry entry = widgetEntries.get(position);
            for (WidgetEntryVisualizer<? extends WidgetEntry> visualizer : visualizers) {
                RemoteViews views = visualizer.getRemoteViews(entry);
                if (views != null) return views;
            }
        }
        logEvent("no view at:" + position);
        return null;
    }

    @NonNull
    private InstanceSettings getSettings() {
        return AllSettings.instanceFromId(context, widgetId);
    }

    @Override
    public void onDataSetChanged() {
        logEvent("onDataSetChanged");
        reload();
    }

    private void reload() {
        if (!AllSettings.isWidgetAllowed(widgetId)) {
            logEvent("reload, skip as the widget is not allowed");
            return;
        }
        long prevReloadMillis = Math.abs(System.currentTimeMillis() - prevReloadFinishedAt);
        if (prevReloadMillis < MIN_MILLIS_BETWEEN_RELOADS) {
            logEvent("reload, skip as done " + prevReloadMillis + " ms ago");
            return;
        }

        InstanceSettings settings = getSettings();
        visualizers = getVisualizers();
        this.widgetEntries = getWidgetEntries(settings);
        logEvent("reload, visualizers:" + visualizers.size() + ", entries:" + this.widgetEntries.size());


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_initial);

            AppWidgetProvider.configureWidgetHeader(settings, rv);
            AppWidgetProvider.configureWidgetEntriesList(settings, context, widgetId, rv);
            configureGotoToday(settings, rv, getTomorrowsPosition(), getTodaysPosition());

            appWidgetManager.updateAppWidget(widgetId, rv);
        } else {
            Log.d(AppWidgetProvider.class.getSimpleName(), widgetId + " reload, appWidgetManager is null" +
                    ", context:" + context);
        }

        prevReloadFinishedAt = System.currentTimeMillis();
    }

    private List<WidgetEntryVisualizer<? extends WidgetEntry>> getVisualizers() {
        List<WidgetEntryVisualizer<? extends WidgetEntry>> visualizers = new ArrayList<>();
        DayHeaderVisualizer dayHeaderVisualizer = new DayHeaderVisualizer(
                getSettings().getContext(),
                widgetId);
        visualizers.add(dayHeaderVisualizer);
        for (EventProviderType type : EventProviderType.values()) {
            if (type.hasEventSources()) {
                visualizers.add(type.getVisualizer(getSettings().getContext(), widgetId));
            }
        }
        visualizers.add(new LastEntryVisualizer(context, widgetId));
        return visualizers;
    }

    private int getTodaysPosition() {
        for (int ind = 0; ind < getWidgetEntries().size() - 1; ind++) {
            if (getWidgetEntries().get(ind).getTimeSection() != TimeSection.PAST) return ind;
        }
        return getWidgetEntries().size() - 1;
    }

    private int getTomorrowsPosition() {
        for (int ind = 0; ind < getWidgetEntries().size() - 1; ind++) {
            if (getWidgetEntries().get(ind).getTimeSection() == TimeSection.FUTURE) return ind;
        }
        return getWidgetEntries().size() > 0 ? 0 : -1;
    }

    private List<WidgetEntry> getWidgetEntries(InstanceSettings settings) {
        List<WidgetEntry> eventEntries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> visualizer : visualizers) {
            eventEntries.addAll(visualizer.getEventEntries());
        }
        Collections.sort(eventEntries);
        List<WidgetEntry> widgetEntries = settings.getShowDayHeaders() ? addDayHeaders(eventEntries) : eventEntries;
        widgetEntries.add(LastEntry.from(settings, widgetEntries));
        return widgetEntries;
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
        logEvent("getViewTypeCount:" + result);
        return result;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    private void configureGotoToday(InstanceSettings settings, RemoteViews rv, int tomorrowsPosition, int todaysPosition) {
        int widgetId = settings.getWidgetId();
        PendingIntent pendingIntent;
        if (todaysPosition < 0) {
            pendingIntent = AppWidgetProvider.getEmptyPendingIntent(context);
        } else {
            Intent intent = new Intent(context.getApplicationContext(), EnvironmentChangedReceiver.class);
            intent.setAction(AppWidgetProvider.ACTION_GOTO_POSITIONS);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.putExtra(AppWidgetProvider.EXTRA_WIDGET_LIST_POSITION1, tomorrowsPosition);
            intent.putExtra(AppWidgetProvider.EXTRA_WIDGET_LIST_POSITION2, todaysPosition);
            pendingIntent = PermissionsUtil.getPermittedPendingBroadcastIntent(settings, intent);
        }
        rv.setOnClickPendingIntent(R.id.go_to_today, pendingIntent);
        logEvent("configureGotoToday, position:" + tomorrowsPosition + " -> " + todaysPosition);
    }
}