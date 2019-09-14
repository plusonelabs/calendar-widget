package org.andstatus.todoagenda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class EnvironmentChangedReceiver extends BroadcastReceiver {
    private static final AtomicReference<EnvironmentChangedReceiver> registeredReceiver = new AtomicReference<>();

    public static void registerReceivers(Map<Integer, InstanceSettings> instances) {
        if (instances.isEmpty()) return;

        InstanceSettings instanceSettings = instances.values().iterator().next();
        Context context = instanceSettings.getContext().getApplicationContext();
        synchronized (registeredReceiver) {
            EnvironmentChangedReceiver receiver = new EnvironmentChangedReceiver();
            EventProviderType.registerProviderChangedReceivers(context, receiver);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                filter.addAction(Intent.ACTION_DREAMING_STOPPED);
            }
            context.registerReceiver(receiver, filter);

            EnvironmentChangedReceiver oldReceiver = registeredReceiver.getAndSet(receiver);
            if (oldReceiver != null) {
                oldReceiver.unRegister(context);
            }
            scheduleNextAlarms(context, instances);

            Log.i(AppWidgetProvider.class.getSimpleName(),
                    "Registered receivers from " + instanceSettings.getContext().getClass().getName());
        }
    }

    private static void scheduleNextAlarms(Context context, Map<Integer, InstanceSettings> instances) {
        Set<DateTime> alarmTimes = new HashSet<>();
        for (InstanceSettings settings : instances.values()) {
            alarmTimes.add(DateUtil.now(settings.getTimeZone()).withTimeAtStartOfDay().plusDays(1));
        }
        int counter = 0;
        for (DateTime alarmTime : alarmTimes) {
            Intent intent = new Intent(context, EnvironmentChangedReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    AppWidgetProvider.REQUEST_CODE_MIDNIGHT_ALARM + counter,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC, alarmTime.getMillis(), pendingIntent);
            counter++;
        }
    }

    public static void forget() {
        registeredReceiver.set(null);
    }

    private void unRegister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.getClass().getSimpleName(), "Received intent: " + intent);
        AllSettings.ensureLoadedFromFiles(context, false);
        String action = intent == null
                ? ""
                : (intent.getAction() == null ? "" : intent.getAction());
        switch (action) {
            case AppWidgetProvider.ACTION_GOTO_POSITIONS:
                int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
                int position1 = intent.getIntExtra(AppWidgetProvider.EXTRA_WIDGET_LIST_POSITION1, 0);
                int position2 = intent.getIntExtra(AppWidgetProvider.EXTRA_WIDGET_LIST_POSITION2, 0);
                gotoPosition(context, widgetId, position1);
                if (position1 >= 0 && position2 >= 0 && position1 != position2) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }
                gotoPosition(context, widgetId, position2);
                break;
            default:
                AppWidgetProvider.recreateAllWidgets(context);
                break;
        }
    }

    private void gotoPosition(Context context, int widgetId, int position) {
        if (widgetId == 0 || position < 0) return;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_parent);
        Log.d("gotoToday", "Scrolling widget " + widgetId + " to position " + position);
        rv.setScrollPosition(R.id.event_list, position);
        appWidgetManager.updateAppWidget(widgetId, rv);
    }
}