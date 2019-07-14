package org.andstatus.todoagenda.task.dmfs;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.andstatus.todoagenda.calendar.CalendarQueryResult;
import org.andstatus.todoagenda.calendar.CalendarQueryResultsStorage;
import org.andstatus.todoagenda.task.AbstractTaskProvider;
import org.andstatus.todoagenda.task.TaskEvent;

import java.util.ArrayList;
import java.util.List;

public class DmfsOpenTasksProvider extends AbstractTaskProvider {

    public DmfsOpenTasksProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    @Override
    public List<TaskEvent> getTasks() {
        if (!hasPermission()) {
            return new ArrayList<>();
        }

        initialiseParameters();

        return queryTasks();
    }

    private List<TaskEvent> queryTasks() {
        Uri uri = DmfsOpenTasksContract.PROVIDER_URI;
        String[] projection = {
                DmfsOpenTasksContract.COLUMN_ID,
                DmfsOpenTasksContract.COLUMN_TITLE,
                DmfsOpenTasksContract.COLUMN_DUE_DATE
        };
        String where = getWhereClause();

        CalendarQueryResult result = new CalendarQueryResult(getSettings(), uri, projection, where, null, null);

        Cursor cursor = context.getContentResolver().query(uri, projection, where, null, null);
        if (cursor == null) {
            return new ArrayList<>();
        }

        List<TaskEvent> tasks = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                if (CalendarQueryResultsStorage.getNeedToStoreResults()) {
                    result.addRow(cursor);
                }

                TaskEvent task = createTask(cursor);
                if (!mKeywordsFilter.matched(task.getTitle())) {
                    tasks.add(task);
                }
            }
        } finally {
            cursor.close();
        }

        CalendarQueryResultsStorage.store(result);

        return tasks;
    }

    private String getWhereClause() {
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(DmfsOpenTasksContract.COLUMN_STATUS).append(NOT_EQUALS).append(DmfsOpenTasksContract.STATUS_COMPLETED);

        whereBuilder.append(AND_BRACKET)
                .append(DmfsOpenTasksContract.COLUMN_DUE_DATE).append(LTE).append(mEndOfTimeRange.getMillis())
                .append(OR)
                .append(DmfsOpenTasksContract.COLUMN_DUE_DATE).append(IS_NULL)
                .append(CLOSING_BRACKET);

        return whereBuilder.toString();
    }

    private TaskEvent createTask(Cursor cursor) {
        TaskEvent task = new DmfsOpenTasksEvent();
        task.setId(cursor.getLong(cursor.getColumnIndex(DmfsOpenTasksContract.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(DmfsOpenTasksContract.COLUMN_TITLE)));

        int dueDateIdx = cursor.getColumnIndex(DmfsOpenTasksContract.COLUMN_DUE_DATE);
        Long dueMillis = null;
        if (!cursor.isNull(dueDateIdx)) {
            dueMillis = cursor.getLong(dueDateIdx);
        }
        task.setStartDate(getDueDate(dueMillis));

        return task;
    }

    @Override
    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, DmfsOpenTasksContract.PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{DmfsOpenTasksContract.PERMISSION}, 1);
    }
}
