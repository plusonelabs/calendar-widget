package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.plusonelabs.calendar.util.PermissionsUtil;

import java.util.List;

public class WidgetConfigurationActivity extends PreferenceActivity {

    private static final String PREFERENCES_PACKAGE_NAME = "com.plusonelabs.calendar.prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PermissionsUtil.arePermissionsGranted(this)) {
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