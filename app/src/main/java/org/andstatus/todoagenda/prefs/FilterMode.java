package org.andstatus.todoagenda.prefs;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;

/**
 * @author yvolk@yurivolkov.com
 */
public enum FilterMode {
    NORMAL_FILTER("normal", R.string.filter_mode_normal),
    /** Include filtering that is usually done at the content provider query level */
    DEBUG_FILTER("debug", R.string.filter_mode_debug),
    NO_FILTERING("no_filtering", R.string.filter_mode_no_filtering);

    public final static FilterMode defaultValue = NORMAL_FILTER;

    public final String value;
    @StringRes
    public final int valueResId;

    FilterMode(String value, int valueResId) {
        this.value = value;
        this.valueResId = valueResId;
    }

    public static FilterMode fromValue(String value) {
        for (FilterMode item : FilterMode.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }
}