package com.plusonelabs.calendar.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class UniquePreferencesFragment extends PreferenceFragment {

    protected int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static final String PACKAGE_NAME = "com.plusonelabs.calendar.prefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            appWidgetId = savedInstanceState.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        } else {
            Bundle extras = getActivity().getIntent().getExtras();
            if (extras != null) {
                appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
            }
        }

        super.onCreate(savedInstanceState);

        PreferenceManager manager = this.getPreferenceManager();
        manager.setSharedPreferencesName(PACKAGE_NAME + appWidgetId);
    }

    public static SharedPreferences getPreferences(Context context, int widgetId) {
        return context.getSharedPreferences(PACKAGE_NAME + widgetId, Context.MODE_PRIVATE);
    }
}
