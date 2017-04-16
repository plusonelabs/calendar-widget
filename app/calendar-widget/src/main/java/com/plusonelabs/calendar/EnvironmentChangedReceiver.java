package com.plusonelabs.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EnvironmentChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_LOCALE_CHANGED)
                || action.equals(Intent.ACTION_TIME_CHANGED)
                || action.equals(Intent.ACTION_DATE_CHANGED)
                || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            EventAppWidgetProvider.updateAllWidgets(context);
        }
        EventAppWidgetProvider.updateEventList(context);
    }
}