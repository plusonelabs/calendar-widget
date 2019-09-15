package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.TextShading;

import androidx.annotation.StringRes;

public enum TextShadingPref {

    WIDGET_HEADER("headerTheme",   TextShading.DARK, R.string.appearance_header_theme_title),
    DAY_HEADER("dayHeaderTheme", TextShading.DARK, R.string.day_header_theme_title),
    ENTRY("entryTheme", TextShading.BLACK, R.string.appearance_entries_theme_title);

    public final String preferenceName;
    public final TextShading defaultShading;
    @StringRes
    public final int titleResId;

    TextShadingPref(String preferenceName, TextShading defaultShading, int titleResId) {
        this.preferenceName = preferenceName;
        this.defaultShading = defaultShading;
        this.titleResId = titleResId;
    }
}
