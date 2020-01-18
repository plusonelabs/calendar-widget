package org.andstatus.todoagenda.widget;

/**
 * On special positions see https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887
 * @author yvolk@yurivolkov.com
 */
public enum WidgetEntryPosition {
    PAST_AND_DUE_HEADER("PastAndDueHeader",false, 1, 1),
    PAST_AND_DUE("PastAndDue",             false, 2, 2),
    DAY_HEADER("DayHeader",                 true, 3, 1),
    START_OF_TODAY("StartOfToday",         false, 3, 2),
    START_OF_DAY("StartOfDay",              true, 3, 3),
    ENTRY_DATE("EntryDate",                 true, 3, 4),
    END_OF_TODAY("EndOfToday",             false, 3, 5),
    END_OF_LIST_HEADER("EndOfListHeader",  false, 5, 1),
    END_OF_LIST("EndOfList",               false, 5, 1),
    LIST_FOOTER("ListFooter",              false, 6, 1),
    HIDDEN("Hidden",                       false, 9, 9),
    UNKNOWN("Unknown",                     false, 9, 9);

    public static WidgetEntryPosition defaultValue = UNKNOWN;

    public final String value;
    final boolean entryDateIsRequired;
    final int globalOrder;
    final int sameDayOrder;

    WidgetEntryPosition(String value, boolean dateIsRequired, int globalOrder, int sameDayOrder) {
        this.value = value;
        this.entryDateIsRequired = dateIsRequired;
        this.globalOrder = globalOrder;
        this.sameDayOrder = sameDayOrder;
    }

    public static WidgetEntryPosition fromValue(String value) {
        for (WidgetEntryPosition item : WidgetEntryPosition.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }
}
