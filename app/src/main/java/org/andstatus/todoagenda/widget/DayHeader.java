package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.joda.time.DateTime;

public class DayHeader extends WidgetEntry<DayHeader> {

    public DayHeader(InstanceSettings settings, WidgetEntryPosition entryPosition, DateTime date) {
        super(settings, entryPosition, date, null);
    }
}
