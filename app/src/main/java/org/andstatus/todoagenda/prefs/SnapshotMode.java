package org.andstatus.todoagenda.prefs;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;

/**
 * @author yvolk@yurivolkov.com
 */
public enum SnapshotMode {
    LIVE_DATA("live_data", R.string.snapshot_mode_live_data),
    SNAPSHOT_TIME("snapshot_time", R.string.snapshot_mode_time),
    SNAPSHOT_NOW("snapshot_now", R.string.snapshot_mode_now);

    public final static SnapshotMode defaultValue = LIVE_DATA;

    public final String value;
    @StringRes
    public final int valueResId;

    SnapshotMode(String value, int valueResId) {
        this.value = value;
        this.valueResId = valueResId;
    }

    public static SnapshotMode fromValue(String value) {
        for (SnapshotMode item : SnapshotMode.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }
}