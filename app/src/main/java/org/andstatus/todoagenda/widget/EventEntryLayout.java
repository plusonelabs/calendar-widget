package org.andstatus.todoagenda.widget;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;

/**
 * @author yvolk@yurivolkov.com
 */
public enum EventEntryLayout {
    DEFAULT(R.layout.event_entry, "DEFAULT", R.string.default_multiline_layout),
    ONE_LINE(R.layout.event_entry_one_line, "ONE_LINE", R.string.single_line_layout);
    public static final String SPACE_PIPE_SPACE = "  |  ";

    @LayoutRes
    public final int layoutId;
    public final String value;
    @StringRes
    public final int summaryResId;

    EventEntryLayout(@LayoutRes int layoutId, String value, int summaryResId) {
        this.layoutId = layoutId;
        this.value = value;
        this.summaryResId = summaryResId;
    }

    public static EventEntryLayout fromValue(String value) {
        EventEntryLayout layout = DEFAULT;
        for (EventEntryLayout item : EventEntryLayout.values()) {
            if (item.value.equals(value)) {
                layout = item;
                break;
            }
        }
        return layout;
    }
}
