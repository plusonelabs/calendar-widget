package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.R;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * @author yvolk@yurivolkov.com
 */
public enum WidgetHeaderLayout {
    ONE_ROW(R.layout.widget_header_one_row, "ONE_ROW", R.string.single_line_layout),
    TWO_ROWS(R.layout.widget_header_two_rows, "TWO_ROWS", R.string.two_rows_layout),
    HIDDEN(0, "HIDDEN", R.string.hidden);

    public static WidgetHeaderLayout defaultValue = ONE_ROW;

    @LayoutRes
    public final int layoutId;
    public final String value;
    @StringRes
    public final int summaryResId;

    WidgetHeaderLayout(@LayoutRes int layoutId, String value, @StringRes int summaryResId) {
        this.layoutId = layoutId;
        this.value = value;
        this.summaryResId = summaryResId;
    }

    public static WidgetHeaderLayout fromValue(String value) {
        for (WidgetHeaderLayout item : WidgetHeaderLayout.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }

}
