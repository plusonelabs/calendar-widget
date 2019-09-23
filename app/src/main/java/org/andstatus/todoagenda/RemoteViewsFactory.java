package org.andstatus.todoagenda;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.CalendarIntentUtil;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.DayHeaderVisualizer;
import org.andstatus.todoagenda.widget.LastEntry;
import org.andstatus.todoagenda.widget.LastEntryVisualizer;
import org.andstatus.todoagenda.widget.TimeSection;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.andstatus.todoagenda.widget.WidgetHeaderLayout;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarPendingIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setAlpha;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setImageFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

public class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = RemoteViewsFactory.class.getSimpleName();

    static final int MIN_MILLIS_BETWEEN_RELOADS = 500;
    private static final int MAX_NUMBER_OF_WIDGETS = 100;
    private static final int REQUEST_CODE_EMPTY = 1;
    private static final int REQUEST_CODE_ADD_EVENT = 2;
    static final int REQUEST_CODE_MIDNIGHT_ALARM = REQUEST_CODE_ADD_EVENT + MAX_NUMBER_OF_WIDGETS;
    static final String EXTRA_WIDGET_LIST_POSITION1 = "widgetListPosition1";
    static final String EXTRA_WIDGET_LIST_POSITION2 = "widgetListPosition2";
    private static final String PACKAGE = "org.andstatus.todoagenda";
    static final String ACTION_GOTO_POSITIONS = PACKAGE + ".action.GOTO_TODAY";
    static final String ACTION_REFRESH = PACKAGE + ".action.REFRESH";

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
        } else {
            visualizers = getVisualizers();
            this.widgetEntries = queryWidgetEntries(getSettings());
            logEvent("reload, visualizers:" + visualizers.size() + ", entries:" + this.widgetEntries.size());
            prevReloadFinishedAt = System.currentTimeMillis();
        }
        updateWidget(context, widgetId, this);
    }

    static void updateWidget(Context context, int widgetId, @Nullable RemoteViewsFactory factory) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetManager == null) {
                Log.d(TAG, widgetId + " updateWidget, appWidgetManager is null, context:" + context);
                return;
            }

            InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_initial);

            configureWidgetHeader(settings, rv);
            configureWidgetEntriesList(settings, context, widgetId, rv);
            if (factory != null) {
                factory.configureGotoToday(settings, rv, factory.getTomorrowsPosition(), factory.getTodaysPosition());
            }
            appWidgetManager.updateAppWidget(widgetId, rv);
        } catch (Exception e) {
            Log.w(TAG, widgetId + " Exception in updateWidget, context:" + context, e);
        }
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

    private List<WidgetEntry> queryWidgetEntries(InstanceSettings settings) {
        List<WidgetEntry> eventEntries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> visualizer : visualizers) {
            eventEntries.addAll(visualizer.queryEventEntries());
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

    private static void configureWidgetHeader(InstanceSettings settings, RemoteViews rv) {
        Log.d(TAG, settings.getWidgetId() + " configureWidgetHeader, layout:" + settings.getWidgetHeaderLayout());
        rv.removeAllViews(R.id.header_parent);
        if (settings.getWidgetHeaderLayout() == WidgetHeaderLayout.HIDDEN) return;

        RemoteViews headerView = new RemoteViews(settings.getContext().getPackageName(),
                settings.getWidgetHeaderLayout().layoutId);
        rv.addView(R.id.header_parent, headerView);

        setBackgroundColor(rv, R.id.action_bar, settings.getWidgetHeaderBackgroundColor());
        configureCurrentDate(settings, rv);
        setActionIcons(settings, rv);
        configureAddEvent(settings, rv);
        configureRefresh(settings, rv);
        configureOverflowMenu(settings, rv);
    }

    private static void configureCurrentDate(InstanceSettings settings, RemoteViews rv) {
        int viewId = R.id.calendar_current_date;
        rv.setOnClickPendingIntent(viewId, createOpenCalendarPendingIntent(settings));
        String formattedDate = settings.getShowDateOnWidgetHeader()
                ? DateUtil.createDateString(settings, DateUtil.now(settings.getTimeZone())).toUpperCase(Locale.getDefault())
                : "                    ";
        rv.setTextViewText(viewId, formattedDate);
        setTextSize(settings, rv, viewId, R.dimen.widget_header_title);
        setTextColorFromAttr(settings.getShadingContext(TextShadingPref.WIDGET_HEADER), rv, viewId, R.attr.header);
    }

    private static void setActionIcons(InstanceSettings settings, RemoteViews rv) {
        ContextThemeWrapper themeContext = settings.getShadingContext(TextShadingPref.WIDGET_HEADER);
        setImageFromAttr(themeContext, rv, R.id.go_to_today, R.attr.header_action_go_to_today);
        setImageFromAttr(themeContext, rv, R.id.add_event, R.attr.header_action_add_event);
        setImageFromAttr(themeContext, rv, R.id.refresh, R.attr.header_action_refresh);
        setImageFromAttr(themeContext, rv, R.id.overflow_menu, R.attr.header_action_overflow);
        TextShading textShading = settings.getShading(TextShadingPref.WIDGET_HEADER);
        int alpha = 255;
        if (textShading == TextShading.DARK || textShading == TextShading.LIGHT) {
            alpha = 154;
        }
        setAlpha(rv, R.id.go_to_today, alpha);
        setAlpha(rv, R.id.add_event, alpha);
        setAlpha(rv, R.id.refresh, alpha);
        setAlpha(rv, R.id.overflow_menu, alpha);
    }

    private static void configureAddEvent(InstanceSettings settings, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.add_event, getPermittedAddEventPendingIntent(settings));
    }

    private static void configureRefresh(InstanceSettings settings, RemoteViews rv) {
        Intent intent = new Intent(settings.getContext(), EnvironmentChangedReceiver.class);
        intent.setAction(ACTION_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.getWidgetId());
        PendingIntent pendingIntent = PermissionsUtil.getPermittedPendingBroadcastIntent(settings, intent);
        rv.setOnClickPendingIntent(R.id.refresh, pendingIntent);
    }

    private static void configureOverflowMenu(InstanceSettings settings, RemoteViews rv) {
        Intent intent = MainActivity.intentToConfigure(settings.getContext(), settings.getWidgetId());
        PendingIntent pendingIntent = PermissionsUtil.getPermittedPendingActivityIntent(settings, intent);
        rv.setOnClickPendingIntent(R.id.overflow_menu, pendingIntent);
    }

    private static void configureWidgetEntriesList(InstanceSettings settings, Context context, int widgetId, RemoteViews rv) {
        Intent intent = new Intent(context, org.andstatus.todoagenda.RemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.event_list, intent);
        boolean permissionsGranted = PermissionsUtil.arePermissionsGranted(context);
        if (permissionsGranted) {
            rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(settings));
        }
    }

    private void configureGotoToday(InstanceSettings settings, RemoteViews rv, int tomorrowsPosition, int todaysPosition) {
        int widgetId = settings.getWidgetId();
        PendingIntent pendingIntent;
        if (todaysPosition < 0) {
            pendingIntent = getEmptyPendingIntent(context);
        } else {
            Intent intent = new Intent(context.getApplicationContext(), EnvironmentChangedReceiver.class);
            intent.setAction(ACTION_GOTO_POSITIONS);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.putExtra(EXTRA_WIDGET_LIST_POSITION1, tomorrowsPosition);
            intent.putExtra(EXTRA_WIDGET_LIST_POSITION2, todaysPosition);
            pendingIntent = PermissionsUtil.getPermittedPendingBroadcastIntent(settings, intent);
        }
        rv.setOnClickPendingIntent(R.id.go_to_today, pendingIntent);
        logEvent("configureGotoToday, position:" + tomorrowsPosition + " -> " + todaysPosition);
    }

    public static PendingIntent getPermittedAddEventPendingIntent(InstanceSettings settings) {
        Context context = settings.getContext();
        Intent intent = PermissionsUtil.getPermittedActivityIntent(context,
                CalendarIntentUtil.createNewEventIntent(settings.getTimeZone()));
        return isIntentAvailable(context, intent) ?
                PendingIntent.getActivity(context, REQUEST_CODE_ADD_EVENT, intent, PendingIntent.FLAG_UPDATE_CURRENT) :
                getEmptyPendingIntent(context);
    }

    private static PendingIntent getEmptyPendingIntent(Context context) {
        return PendingIntent.getActivity(
                context.getApplicationContext(),
                REQUEST_CODE_EMPTY,
                new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}