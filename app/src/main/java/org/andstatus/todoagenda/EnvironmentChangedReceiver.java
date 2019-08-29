package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.provider.EventProviderType;

import java.util.concurrent.atomic.AtomicReference;

public class EnvironmentChangedReceiver extends BroadcastReceiver {
    private static final AtomicReference<EnvironmentChangedReceiver> registeredReceiver = new AtomicReference<>();

    public static void registerReceivers(Context contextIn) {
        Context context = contextIn.getApplicationContext();
        synchronized (registeredReceiver) {
            EnvironmentChangedReceiver receiver = new EnvironmentChangedReceiver();
            EventProviderType.registerProviderChangedReceivers(context, receiver);

            IntentFilter userPresent = new IntentFilter();
            userPresent.addAction("android.intent.action.USER_PRESENT");
            context.registerReceiver(receiver, userPresent);

            EnvironmentChangedReceiver oldReceiver = registeredReceiver.getAndSet(receiver);
            if (oldReceiver != null) {
                oldReceiver.unRegister(context);
            }

            Log.i(EventAppWidgetProvider.class.getName(),
                    "Registered receivers from " + contextIn.getClass().getName());
        }
    }

    private void unRegister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.getClass().getName(), "Received intent: " + intent);
        AllSettings.ensureLoadedFromFiles(context, false);
        String action = intent == null
                ? ""
                : (intent.getAction() == null ? "" : intent.getAction());
        switch (action) {
            case Intent.ACTION_LOCALE_CHANGED:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
                EventAppWidgetProvider.updateWidgetsWithData(context);
                break;
            case EventAppWidgetProvider.ACTION_GOTO_POSITIONS:
                int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
                int position1 = intent.getIntExtra(EventAppWidgetProvider.EXTRA_WIDGET_LIST_POSITION1, 0);
                int position2 = intent.getIntExtra(EventAppWidgetProvider.EXTRA_WIDGET_LIST_POSITION2, 0);
                gotoPosition(context, widgetId, position1);
                if (position1 >= 0 && position2 >= 0 && position1 != position2) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                gotoPosition(context, widgetId, position2);
                break;
            default:
                EventAppWidgetProvider.updateEventList(context);
                break;
        }
    }

    private void gotoPosition(Context context, int widgetId, int position) {
        if (widgetId == 0 || position < 0) return;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        Log.d("gotoToday", "Scrolling widget " + widgetId + " to position " + position);
        rv.setScrollPosition(R.id.event_list, position);
        appWidgetManager.updateAppWidget(widgetId, rv);
    }
}