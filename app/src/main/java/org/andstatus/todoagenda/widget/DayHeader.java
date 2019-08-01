package org.andstatus.todoagenda.widget;

import org.joda.time.DateTime;

public class DayHeader extends WidgetEntry<DayHeader> {

    public DayHeader(DateTime date) {
        setStartDate(date.withTimeAtStartOfDay());
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
