package org.andstatus.todoagenda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

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
            default:
                EventAppWidgetProvider.updateEventList(context);
                break;
        }
    }
}