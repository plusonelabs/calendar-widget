package org.andstatus.todoagenda;

import androidx.annotation.LayoutRes;

/**
 * @author yvolk@yurivolkov.com
 */
public enum AlarmIndicatorScaled {
    VERY_SMALL(R.id.event_entry_indicator_alarm_very_small),
    SMALL(R.id.event_entry_indicator_alarm_small),
    MEDIUM(R.id.event_entry_indicator_alarm);

    @LayoutRes
    public final int indicatorResId;

    AlarmIndicatorScaled(int indicatorResId) {
        this.indicatorResId = indicatorResId;
    }
}
