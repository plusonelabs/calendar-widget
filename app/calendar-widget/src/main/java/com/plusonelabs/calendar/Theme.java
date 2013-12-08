package com.plusonelabs.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_THEME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_THEME_DEFAULT;

public enum Theme {

	LIGHT,
	DARK;

    public static int getCurrentThemeId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Theme theme = Theme.valueOf(prefs.getString(PREF_THEME, PREF_THEME_DEFAULT));
        int themeId = R.style.Theme_Calendar;
        if (theme == Theme.LIGHT) {
            themeId = R.style.Theme_Calendar_Light;
        }
        return themeId;
    }

}
