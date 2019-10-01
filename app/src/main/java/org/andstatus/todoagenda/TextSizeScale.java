package org.andstatus.todoagenda;

/**
 * @author yvolk@yurivolkov.com
 */
public enum TextSizeScale {
    VERY_SMALL("0.6", 0.6f,
            AlarmIndicatorScaled.VERY_SMALL, RecurringIndicatorScaled.VERY_SMALL),
    SMALL("0.8", 0.8f,
            AlarmIndicatorScaled.SMALL, RecurringIndicatorScaled.SMALL),
    MEDIUM("1.0", 1.0f,
            AlarmIndicatorScaled.MEDIUM, RecurringIndicatorScaled.MEDIUM),
    LARGE("1.25", 1.25f,
            AlarmIndicatorScaled.MEDIUM, RecurringIndicatorScaled.MEDIUM),
    VERY_LARGE("1.75", 1.75f,
            AlarmIndicatorScaled.MEDIUM, RecurringIndicatorScaled.MEDIUM);

    public final String preferenceValue;
    public final float scaleValue;
    public final AlarmIndicatorScaled alarmIndicator;
    public final RecurringIndicatorScaled recurringIndicator;

    TextSizeScale(String preferenceValue, float scaleValue,
                  AlarmIndicatorScaled alarmIndicator, RecurringIndicatorScaled recurringIndicator) {
        this.preferenceValue = preferenceValue;
        this.scaleValue = scaleValue;
        this.alarmIndicator = alarmIndicator;
        this.recurringIndicator = recurringIndicator;
    }

    public static TextSizeScale fromPreferenceValue(String preferenceValue) {
        for (TextSizeScale item : TextSizeScale.values()) {
            if (item.preferenceValue.equals(preferenceValue)) {
                return item;
            }
        }
        return MEDIUM;
    }
}
