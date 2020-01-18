package org.andstatus.todoagenda.prefs;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.widget.WidgetEntryPosition;

/**
 * Where to show tasks without start and due dates
 * See https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887
 * @author yvolk@yurivolkov.com
 */
public enum TasksWithoutDates {
    END_OF_LIST( "end_of_list",  WidgetEntryPosition.END_OF_LIST,  R.string.tasks_wo_dates_end_of_list),
    END_OF_TODAY("end_of_today", WidgetEntryPosition.END_OF_TODAY, R.string.tasks_wo_dates_end_of_today),
    HIDE(        "hide",         WidgetEntryPosition.HIDDEN,       R.string.tasks_wo_dates_hide);

    public final static TasksWithoutDates defaultValue = END_OF_LIST;

    public final String value;
    @StringRes
    public final int valueResId;
    public final WidgetEntryPosition widgetEntryPosition;

    TasksWithoutDates(String value, WidgetEntryPosition widgetEntryPosition, int valueResId) {
        this.value = value;
        this.widgetEntryPosition = widgetEntryPosition;
        this.valueResId = valueResId;
    }

    public static TasksWithoutDates fromValue(String value) {
        for (TasksWithoutDates item : TasksWithoutDates.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }
}