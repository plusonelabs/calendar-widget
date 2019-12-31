package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.joda.time.DateTime;

import static org.andstatus.todoagenda.widget.WidgetEntryPosition.DAY_HEADER;

public class DayHeader extends WidgetEntry<DayHeader> {

    public DayHeader(InstanceSettings settings, DateTime date) {
        super(settings, DAY_HEADER, date);
    }
}
