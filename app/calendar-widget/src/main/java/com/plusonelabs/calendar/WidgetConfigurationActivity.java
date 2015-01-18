package com.plusonelabs.calendar;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.PreferenceActivity;

import java.util.List;

public class WidgetConfigurationActivity extends PreferenceActivity {

    private static final String PREFERENCES_PACKAGE_NAME = "com.plusonelabs.calendar.prefs";

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