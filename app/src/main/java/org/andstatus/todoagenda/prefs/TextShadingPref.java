package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.TextShading;
import org.andstatus.todoagenda.widget.TimeSection;
import org.andstatus.todoagenda.widget.WidgetEntry;

import androidx.annotation.StringRes;

public enum TextShadingPref {

    WIDGET_HEADER("headerTheme",   TextShading.DARK, R.string.appearance_header_theme_title,
            false, TimeSection.ALL),
    DAY_HEADER_PAST("dayHeaderThemePast", TextShading.DARK, R.string.day_header_theme_title,
            true, TimeSection.PAST),
    ENTRY_PAST("entryThemePast", TextShading.BLACK, R.string.appearance_entries_theme_title,
            false, TimeSection.PAST),
    DAY_HEADER_TODAY("dayHeaderTheme", TextShading.LIGHT, R.string.day_header_theme_title,
            true, TimeSection.TODAY),
    ENTRY_TODAY("entryTheme", TextShading.WHITE, R.string.appearance_entries_theme_title,
            false, TimeSection.TODAY),
    DAY_HEADER_FUTURE("dayHeaderThemeFuture", TextShading.DARK, R.string.day_header_theme_title,
            true, TimeSection.FUTURE),
    ENTRY_FUTURE("entryThemeFuture", TextShading.BLACK, R.string.appearance_entries_theme_title,
            false, TimeSection.FUTURE);

    public final String preferenceName;
    public final TextShading defaultShading;
    @StringRes
    public final int titleResId;
    public final boolean dependsOnDayHeader;
    public final TimeSection timeSection;

    TextShadingPref(String preferenceName, TextShading defaultShading, int titleResId,
                    boolean dependsOnDayHeader, TimeSection timeSection) {
        this.preferenceName = preferenceName;
        this.defaultShading = defaultShading;
        this.titleResId = titleResId;
        this.dependsOnDayHeader = dependsOnDayHeader;
        this.timeSection = timeSection;
    }

    public static TextShadingPref getDayHeader(WidgetEntry<?> entry) {
        return entry.getTimeSection().select(DAY_HEADER_PAST, DAY_HEADER_TODAY, DAY_HEADER_FUTURE);
    }

    public static TextShadingPref getEntry(WidgetEntry<?> entry) {
        return entry.getTimeSection().select(ENTRY_PAST, ENTRY_TODAY, ENTRY_FUTURE);
    }

}
