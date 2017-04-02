package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.plusonelabs.calendar.prefs.ApplicationPreferences;
import com.plusonelabs.calendar.util.PermissionsUtil;

import java.util.List;

public class WidgetConfigurationActivity extends PreferenceActivity {

    private static final String PREFERENCES_PACKAGE_NAME = "com.plusonelabs.calendar.prefs";
    private int widgetId = 0;

    @NonNull
    public static Intent newIntentToStartMe(Context context, int widgetId) {
        Intent intent = new Intent(context, WidgetConfigurationActivity.class);
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
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        if (widgetId == 0) {
            widgetId = ApplicationPreferences.getWidgetId(this);
        } else {
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
        if (widgetId != 0) {
            setTitle(ApplicationPreferences.getWidgetInstanceName(this));
        }
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