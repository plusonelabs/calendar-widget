package com.plusonelabs.calendar;

import static com.plusonelabs.calendar.prefs.ApplicationPreferences.PREF_ENTRY_THEME_DEFAULT;

public enum Theme {

    WHITE(R.style.Theme_Calendar_White),
    LIGHT(R.style.Theme_Calendar_Light),
    DARK(R.style.Theme_Calendar_Dark),
    BLACK(R.style.Theme_Calendar_Black);

    private final int themeResId;

    Theme(int themeResId) {
        this.themeResId = themeResId;
    }

    public static int themeNameToResId(String themeName) {
        try {
            return Theme.valueOf(themeName).themeResId;
        } catch (Exception e) {
            return Theme.valueOf(PREF_ENTRY_THEME_DEFAULT).themeResId;
        }
    }

}
