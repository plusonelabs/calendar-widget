package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViewsService;

import org.andstatus.todoagenda.prefs.AllSettings;

import static org.andstatus.todoagenda.Theme.themeNameToResId;

public class EventWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Context appContext = getApplicationContext();
        int currentThemeId = themeNameToResId(AllSettings.instanceFromId(appContext, widgetId).getEntryTheme());
        ContextThemeWrapper context = new ContextThemeWrapper(appContext, currentThemeId);
        return new EventRemoteViewsFactory(context, widgetId);
    }
}