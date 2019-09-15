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
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.DayHeaderVisualizer;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int widgetId;
    private volatile List<? extends WidgetEntry> widgetEntries = new ArrayList<>();
    private volatile List<WidgetEntryVisualizer<? extends WidgetEntry>> visualizers = new ArrayList<>();

    public RemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
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
                RemoteViews views = visualizer.getRemoteView(entry);
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

        visualizers = getVisualizers();
        widgetEntries = getSettings().getShowDayHeaders() ? addDayHeaders(getEventEntries()) : getEventEntries();
        logEvent("reload, visualizers:" + visualizers.size() + ", count:" + widgetEntries.size());
        configureGotoToday(getSettings(), getTomorrowsPosition(), getTodaysPosition());
    }

    private List<WidgetEntryVisualizer<? extends WidgetEntry>> getVisualizers() {
        List<WidgetEntryVisualizer<? extends WidgetEntry>> visualizers = new ArrayList<>();
        DayHeaderVisualizer dayHeaderVisualizer = new DayHeaderVisualizer(
                getSettings().getShadingContext(TextShadingPref.DAY_HEADER),
                widgetId);
        visualizers.add(dayHeaderVisualizer);
        for (EventProviderType type : EventProviderType.values()) {
            if (type.hasEventSources()) {
                visualizers.add(type.getVisualizer(getSettings().getShadingContext(TextShadingPref.ENTRY), widgetId));
            }
        }
        return visualizers;
    }

    private int getTodaysPosition() {
        for (int ind = 0; ind < getWidgetEntries().size() - 1; ind++) {
            if (!getWidgetEntries().get(ind).isBeforeToday()) return ind;
        }
        return getWidgetEntries().size() - 1;
    }

    private int getTomorrowsPosition() {
        for (int ind = 0; ind < getWidgetEntries().size() - 1; ind++) {
            if (getWidgetEntries().get(ind).isAfterToday()) return ind;
        }
        return getWidgetEntries().size() > 0 ? 0 : -1;
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
        logEvent("getViewTypeCount:" + result);
        return result;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    private static void configureGotoToday(InstanceSettings settings, int tomorrowsPosition, int todaysPosition) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(settings.getContext());
        if (appWidgetManager == null) return;

        RemoteViews rv = new RemoteViews(settings.getContext().getPackageName(), R.layout.widget_initial);
        int widgetId = settings.getWidgetId();
        PendingIntent pendingIntent;
        if (todaysPosition < 0) {
            pendingIntent = AppWidgetProvider.getEmptyPendingIntent(settings.getContext());
        } else {
            Intent intent = new Intent(settings.getContext().getApplicationContext(), EnvironmentChangedReceiver.class);
            intent.setAction(AppWidgetProvider.ACTION_GOTO_POSITIONS);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.putExtra(AppWidgetProvider.EXTRA_WIDGET_LIST_POSITION1, tomorrowsPosition);
            intent.putExtra(AppWidgetProvider.EXTRA_WIDGET_LIST_POSITION2, todaysPosition);
            pendingIntent = PermissionsUtil.getPermittedPendingBroadcastIntent(settings, intent);
        }
        rv.setOnClickPendingIntent(R.id.go_to_today, pendingIntent);
        Log.d( RemoteViewsFactory.class.getSimpleName(), widgetId +" configureGotoToday" +
                ", position:" + tomorrowsPosition + " -> " + todaysPosition);
        appWidgetManager.updateAppWidget(widgetId, rv);
    }
}
