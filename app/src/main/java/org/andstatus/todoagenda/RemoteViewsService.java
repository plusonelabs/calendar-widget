package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;

import org.andstatus.todoagenda.prefs.AllSettings;

public class RemoteViewsService extends android.widget.RemoteViewsService {
    private static final String TAG = RemoteViewsService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        AllSettings.ensureLoadedFromFiles(this, false);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Log.d(TAG, widgetId + " onGetViewFactory, intent:" + intent);
        return new org.andstatus.todoagenda.RemoteViewsFactory(this, widgetId, true);
    }
}