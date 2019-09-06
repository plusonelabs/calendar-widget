package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;

import org.andstatus.todoagenda.prefs.AllSettings;

public class RemoteViewsService extends android.widget.RemoteViewsService {

    @Override
    public void onCreate() {
        Log.d(this.getClass().getSimpleName(), "onCreate");
        AllSettings.ensureLoadedFromFiles(this, false);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Log.d(this.getClass().getSimpleName(), "onGetViewFactory, widgetId:" + widgetId + ", intent:" + intent);
        return new org.andstatus.todoagenda.RemoteViewsFactory(getApplicationContext(), widgetId);
    }
}