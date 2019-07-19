package org.andstatus.todoagenda.task.dmfs;

import android.content.ContentUris;
import android.content.Intent;

import org.andstatus.todoagenda.CalendarIntentUtil;
import org.andstatus.todoagenda.task.TaskEvent;
import org.joda.time.DateTimeZone;

public class DmfsOpenTasksEvent extends TaskEvent {
    public DmfsOpenTasksEvent(DateTimeZone zone) {
        super(zone);
    }

    @Override
    public Intent createOpenCalendarEventIntent() {
        Intent intent = CalendarIntentUtil.createCalendarIntent();
        intent.setData(ContentUris.withAppendedId(DmfsOpenTasksContract.Tasks.PROVIDER_URI, getId()));
        return intent;
    }
}
