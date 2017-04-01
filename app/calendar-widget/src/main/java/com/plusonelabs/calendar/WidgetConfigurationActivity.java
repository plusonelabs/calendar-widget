package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.plusonelabs.calendar.prefs.ApplicationPreferences;
import com.plusonelabs.calendar.util.PermissionsUtil;

import java.util.List;

public class WidgetConfigurationActivity extends PreferenceActivity {

    private static final String PREFERENCES_PACKAGE_NAME = "com.plusonelabs.calendar.prefs";

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationPreferences.save(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        if (widgetId == 0) {
            widgetId = ApplicationPreferences.getWidgetId(this);
            Log.i(this.getClass().getSimpleName(), "Continue editing " + widgetId);
        } else {
            Log.i(this.getClass().getSimpleName(), "Starting editing " + widgetId);
            ApplicationPreferences.startEditing(this, widgetId);
        }
        super.onCreate(savedInstanceState);
        if (widgetId == 0 || !PermissionsUtil.arePermissionsGranted(this)) {
            startActivity(MainActivity.newIntentToStartMe(this));
            finish();
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_header, target);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected boolean isValidFragment(String fragmentName) {
        if (fragmentName.startsWith(PREFERENCES_PACKAGE_NAME)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }
}