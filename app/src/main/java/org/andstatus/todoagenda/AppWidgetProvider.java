package org.andstatus.todoagenda;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.util.CalendarIntentUtil;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.andstatus.todoagenda.widget.WidgetHeaderLayout;

import java.util.AbstractList;
import java.util.List;
import java.util.Locale;

import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarPendingIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setAlpha;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setImageFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    private static final String TAG = AppWidgetProvider.class.getSimpleName();
    private static final String PACKAGE = "org.andstatus.todoagenda";
    public static final String ACTION_REFRESH = PACKAGE + ".action.REFRESH";
    public static final String ACTION_GOTO_POSITIONS = PACKAGE + ".action.GOTO_TODAY";
    public static final String EXTRA_WIDGET_LIST_POSITION1 = "widgetListPosition1";
    public static final String EXTRA_WIDGET_LIST_POSITION2 = "widgetListPosition2";
    public static final int MAX_NUMBER_OF_WIDGETS = 100;
    public static final int REQUEST_CODE_EMPTY = 1;
    public static final int REQUEST_CODE_ADD_EVENT = 2;
    public static final int REQUEST_CODE_MIDNIGHT_ALARM = REQUEST_CODE_ADD_EVENT + MAX_NUMBER_OF_WIDGETS;

    public AppWidgetProvider() {
        super();
        Log.d(TAG, "init");
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int widgetId, Bundle newOptions) {
        Log.d(TAG, widgetId + " onOptionsChanged");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, widgetId, newOptions);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive, intent:" + intent);
        AllSettings.ensureLoadedFromFiles(context, false);

        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            Bundle extras = intent.getExtras();
            int[] widgetIds = extras == null
                    ? null
                    : extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (widgetIds == null || widgetIds.length == 0) {
                widgetIds = getWidgetIds(context);
                Log.d(TAG, "onUpdate, input: no widgetIds, discovered here:" +
                        asList(widgetIds) + ", context:" + context);
            }
            if (widgetIds != null && widgetIds.length > 0) {
                onUpdate(context, AppWidgetManager.getInstance(context), widgetIds);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled, context:" + context);
        super.onEnabled(context);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Log.d(TAG, "onRestored, oldWidgetIds:" + asList(oldWidgetIds) +
                ", newWidgetIds:" + asList(newWidgetIds));
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted, widgetIds:" + asList(appWidgetIds));
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            AllSettings.delete(context, widgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate, widgetIds:" + asList(appWidgetIds) + ", context:" + context);
        for (int widgetId : appWidgetIds) {
            try {
                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_initial);
                InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
                configureWidgetEntriesList(settings, context, widgetId, rv);
                appWidgetManager.updateAppWidget(widgetId, rv);

                notifyWidgetDataChanged(context, widgetId);
            } catch (Exception e) {
                Log.w(TAG, widgetId + " Exception in onUpdate, context:" + context, e);
            }
        }
    }

    public static void configureWidgetHeader(InstanceSettings settings, RemoteViews rv) {
        Log.d(TAG, settings.getWidgetId() + " configureWidgetHeader" +
                ", layout:" + settings.getWidgetHeaderLayout());
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

    public static PendingIntent getPermittedAddEventPendingIntent(InstanceSettings settings) {
        Context context = settings.getContext();
        Intent intent = PermissionsUtil.getPermittedActivityIntent(context,
                CalendarIntentUtil.createNewEventIntent(settings.getTimeZone()));
        return isIntentAvailable(context, intent) ?
                PendingIntent.getActivity(context, REQUEST_CODE_ADD_EVENT, intent, PendingIntent.FLAG_UPDATE_CURRENT) :
                getEmptyPendingIntent(context);
    }

    public static PendingIntent getEmptyPendingIntent(Context context) {
        return PendingIntent.getActivity(
                context.getApplicationContext(),
                REQUEST_CODE_EMPTY,
                new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
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

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void configureWidgetEntriesList(InstanceSettings settings, Context context, int widgetId, RemoteViews rv) {
        Intent intent = new Intent(context, RemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.event_list, intent);
        boolean permissionsGranted = PermissionsUtil.arePermissionsGranted(context);
        if (permissionsGranted) {
            rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(settings));
        }
    }

    public static List<Integer> asList(final int[] is) {
        return new AbstractList<Integer>() {
            public Integer get(int i) { return is[i]; }
            public int size() { return is.length; }
        };
    }

    public static int[] getWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager == null
                ? new int[]{}
                : appWidgetManager.getAppWidgetIds(new ComponentName(context, AppWidgetProvider.class));
    }

    private static void notifyWidgetDataChanged(Context context, int widgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            appWidgetManager.notifyAppWidgetViewDataChanged(new int[]{widgetId}, R.id.event_list);
        } else {
            Log.d(TAG, widgetId + " notifyWidgetDataChanged, appWidgetManager is null, context:" + context);
        }
    }
}
