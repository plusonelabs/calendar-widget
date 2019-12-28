package org.andstatus.todoagenda.widget;

/**
 * On special positions see https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887
 * @author yvolk@yurivolkov.com
 */
public enum WidgetEntryPosition {
    PAST_AND_DUE_HEADER("PastAndDueHeader",false, 1, 1),
    DAY_HEADER("DayHeader",                 true, 2, 1),
    START_OF_TODAY("StartOfToday",         false, 2, 2),
    START_OF_DAY("StartOfDay",              true, 2, 3),
    ENTRY_DATE("EntryDate",                 true, 2, 4),
    END_OF_TODAY("EndOfToday",             false, 2, 5),
    END_OF_LIST_HEADER("EndOfListHeader",  false, 3, 1),
    END_OF_LIST("EndOfList",               false, 4, 1),
    LIST_FOOTER("ListFooter",              false, 5, 1);

    final String value;
    final boolean entryDateIsRequired;
    final int globalOrder;
    final int sameDayOrder;

    WidgetEntryPosition(String value, boolean dateIsRequired, int globalOrder, int sameDayOrder) {
        this.value = value;
        this.entryDateIsRequired = dateIsRequired;
        this.globalOrder = globalOrder;
        this.sameDayOrder = sameDayOrder;
    }
}
