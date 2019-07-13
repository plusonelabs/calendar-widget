package org.andstatus.todoagenda.task.samsung;

import android.content.ContentUris;
import android.content.Intent;
import android.provider.CalendarContract;

import org.andstatus.todoagenda.CalendarIntentUtil;
import org.andstatus.todoagenda.task.TaskEvent;

public class SamsungTaskEvent extends TaskEvent {

    @Override
    public Intent createOpenCalendarEventIntent() {
        Intent intent = CalendarIntentUtil.createCalendarIntent();
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, getId()));
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_TASK, true);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_SELECTED, getId());
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_ACTION_VIEW_FOCUS, 0);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_DETAIL_MODE, true);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_LAUNCH_FROM_WIDGET, true);
        return intent;
    }
}
