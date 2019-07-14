package org.andstatus.todoagenda.task.dmfs;

import android.content.ContentUris;
import android.content.Intent;
import org.andstatus.todoagenda.CalendarIntentUtil;
import org.andstatus.todoagenda.task.TaskEvent;

public class DmfsOpenTasksEvent extends TaskEvent {
    @Override
    public Intent createOpenCalendarEventIntent() {
        Intent intent = CalendarIntentUtil.createCalendarIntent();
        intent.setData(ContentUris.withAppendedId(DmfsOpenTasksContract.PROVIDER_URI, getId()));
        return intent;
    }
}
