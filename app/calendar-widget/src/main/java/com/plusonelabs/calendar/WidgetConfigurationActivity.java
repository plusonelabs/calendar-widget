package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;

import com.plusonelabs.calendar.prefs.ApplicationPreferences;
import com.plusonelabs.calendar.util.PermissionsUtil;

import java.util.List;

public class WidgetConfigurationActivity extends PreferenceActivity {

    private static final String PREFERENCES_PACKAGE_NAME = "com.plusonelabs.calendar.prefs";
    private int widgetId = 0;

    @NonNull
    public static Intent intentToStartMe(Context context, int widgetId) {
        Intent intent = new Intent(context, WidgetConfigurationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationPreferences.save(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (preparedForNewIntent(getIntent())) {
            super.onCreate(savedInstanceState);
        }
    }

    private boolean preparedForNewIntent(Intent newIntent) {
        int newWidgetId = newIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        if (newWidgetId == 0) {
            newWidgetId = ApplicationPreferences.getWidgetId(this);
        }
        Intent restartIntent = null;
        if (newWidgetId == 0 || !PermissionsUtil.arePermissionsGranted(this)) {
            restartIntent = MainActivity.intentToStartMe(this);
        } else if (widgetId != 0 && widgetId != newWidgetId) {
            restartIntent = MainActivity.intentToConfigure(this, newWidgetId);
        } else if (widgetId == 0) {
            widgetId = newWidgetId;
            ApplicationPreferences.startEditing(this, widgetId);
        }
        if (restartIntent != null) {
            finish();
            startActivity(restartIntent);
        }
        return restartIntent == null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (preparedForNewIntent(getIntent())) {
            super.onNewIntent(intent);
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_header, target);
        setTitle(ApplicationPreferences.getWidgetInstanceName(this));
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