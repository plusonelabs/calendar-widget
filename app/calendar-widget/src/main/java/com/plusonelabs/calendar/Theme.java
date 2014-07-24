package com.plusonelabs.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Theme {

    WHITE(R.style.Theme_Calendar_White),
    LIGHT(R.style.Theme_Calendar_Light),
    DARK(R.style.Theme_Calendar_Dark),
    BLACK(R.style.Theme_Calendar_Black);

    private final int themeResId;

    private Theme(int themeResId) {
        this.themeResId = themeResId;
    }

    public static int getCurrentThemeId(Context context, String prefKey, String prefDefault, SharedPreferences prefs) {
        return Theme.valueOf(prefs.getString(prefKey, prefDefault)).themeResId;
    }

}
