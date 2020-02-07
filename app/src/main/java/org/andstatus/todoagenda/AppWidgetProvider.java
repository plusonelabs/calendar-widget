package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.andstatus.todoagenda.prefs.AllSettings;

import java.util.AbstractList;
import java.util.List;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {
    private static final String TAG = AppWidgetProvider.class.getSimpleName();

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
            RemoteViewsFactory.updateWidget(context, widgetId);
            InstanceState.updated(widgetId);
            appWidgetManager.notifyAppWidgetViewDataChanged(new int[]{widgetId}, R.id.event_list);
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
}
