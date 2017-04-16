package com.plusonelabs.calendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViewsService;

import com.plusonelabs.calendar.prefs.InstanceSettings;

import static com.plusonelabs.calendar.Theme.themeNameToResId;

public class EventWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Context appContext = getApplicationContext();
        int currentThemeId = themeNameToResId(InstanceSettings.fromId(appContext, widgetId).getEntryTheme());
        ContextThemeWrapper context = new ContextThemeWrapper(appContext, currentThemeId);
        return new EventRemoteViewsFactory(context, widgetId);
    }
}