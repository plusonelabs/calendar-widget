package org.andstatus.todoagenda.prefs;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;

/**
 * What date gets main attention for a task
 * See https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887
 * @author yvolk@yurivolkov.com
 */
public enum TaskScheduling {
    DATE_DUE("date_due", R.string.task_scheduling_date_due),
    DATE_STARTED("date_started", R.string.task_scheduling_date_started);

    public final static TaskScheduling defaultValue = DATE_DUE;

    public final String value;
    @StringRes
    public final int valueResId;

    TaskScheduling(String value, int valueResId) {
        this.value = value;
        this.valueResId = valueResId;
    }

    public static TaskScheduling fromValue(String value) {
        for (TaskScheduling item : TaskScheduling.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }
}