package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.TextShading;
import org.andstatus.todoagenda.widget.WidgetEntry;

import androidx.annotation.StringRes;

public enum TextShadingPref {

    WIDGET_HEADER("headerTheme",   TextShading.DARK, R.string.appearance_header_theme_title),
    DAY_HEADER_PAST("dayHeaderThemePast", TextShading.DARK, R.string.day_header_theme_title),
    DAY_HEADER("dayHeaderTheme", TextShading.DARK, R.string.day_header_theme_title),
    DAY_HEADER_FUTURE("dayHeaderThemeFuture", TextShading.DARK, R.string.day_header_theme_title),
    ENTRY_PAST("entryThemePast", TextShading.BLACK, R.string.appearance_entries_theme_title),
    ENTRY("entryTheme", TextShading.BLACK, R.string.appearance_entries_theme_title),
    ENTRY_FUTURE("entryThemeFuture", TextShading.BLACK, R.string.appearance_entries_theme_title);

    public final String preferenceName;
    public final TextShading defaultShading;
    @StringRes
    public final int titleResId;

    TextShadingPref(String preferenceName, TextShading defaultShading, int titleResId) {
        this.preferenceName = preferenceName;
        this.defaultShading = defaultShading;
        this.titleResId = titleResId;
    }

    public static TextShadingPref getDayHeader(WidgetEntry<?> entry) {
        return entry.getTimeSection().select(DAY_HEADER_PAST, DAY_HEADER, DAY_HEADER_FUTURE);
    }

    public static TextShadingPref getEntry(WidgetEntry<?> entry) {
        return entry.getTimeSection().select(ENTRY_PAST, ENTRY, ENTRY_FUTURE);
    }

}
