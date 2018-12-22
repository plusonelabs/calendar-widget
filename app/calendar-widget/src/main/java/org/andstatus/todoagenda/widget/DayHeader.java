package org.andstatus.todoagenda.widget;

import org.joda.time.DateTime;

public class DayHeader extends WidgetEntry {

    public DayHeader(DateTime date) {
        setStartDate(date.withTimeAtStartOfDay());
    }

}
