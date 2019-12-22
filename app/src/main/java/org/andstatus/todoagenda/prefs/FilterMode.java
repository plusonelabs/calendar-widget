package org.andstatus.todoagenda.prefs;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;

/**
 * @author yvolk@yurivolkov.com
 */
public enum FilterMode {
    NORMAL_FILTER("normal", R.string.filter_mode_normal),
    DEBUG_FILTER("debug", R.string.filter_mode_debug),
    NO_FILTERING("no_filtering", R.string.filter_mode_no_filtering);

    public final String value;
    @StringRes
    public final int summaryResId;

    FilterMode(String value, int summaryResId) {
        this.value = value;
        this.summaryResId = summaryResId;
    }

    public static FilterMode fromValue(String value) {
        for (FilterMode item : FilterMode.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return NORMAL_FILTER;
    }
}