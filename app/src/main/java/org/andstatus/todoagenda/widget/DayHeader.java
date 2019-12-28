package org.andstatus.todoagenda.widget;

import org.joda.time.DateTime;

import static org.andstatus.todoagenda.widget.WidgetEntryPosition.DAY_HEADER;

public class DayHeader extends WidgetEntry<DayHeader> {

    public DayHeader(DateTime date) {
        super(DAY_HEADER, date);
    }
}
