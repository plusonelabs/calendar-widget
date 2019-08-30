package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Intent;

import org.andstatus.todoagenda.prefs.AllSettings;

public class RemoteViewsService extends android.widget.RemoteViewsService {

    @Override
    public void onCreate() {
        AllSettings.ensureLoadedFromFiles(this, true);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        return new org.andstatus.todoagenda.RemoteViewsFactory(getApplicationContext(), widgetId);
    }
}