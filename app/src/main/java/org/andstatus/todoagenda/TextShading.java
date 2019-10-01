package org.andstatus.todoagenda;

import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

/**
 * @author yvolk@yurivolkov.com
*/
public enum TextShading {

    WHITE(R.style.Theme_Calendar_White, R.string.appearance_theme_white),
    LIGHT(R.style.Theme_Calendar_Light, R.string.appearance_theme_light),
    DARK(R.style.Theme_Calendar_Dark, R.string.appearance_theme_dark),
    BLACK(R.style.Theme_Calendar_Black, R.string.appearance_theme_black);

    @StyleRes
    public final int themeResId;
    @StringRes
    public final int titleResId;

    TextShading(int themeResId, int titleResId) {
        this.themeResId = themeResId;
        this.titleResId = titleResId;
    }

    public static TextShading fromName(String themeName, TextShading defaultShading) {
        try {
            return TextShading.valueOf(themeName);
        } catch (Exception e) {
            return defaultShading;
        }
    }

}
