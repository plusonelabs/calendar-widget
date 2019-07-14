package org.andstatus.todoagenda.task.samsung;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.andstatus.todoagenda.calendar.CalendarQueryResult;
import org.andstatus.todoagenda.calendar.CalendarQueryResultsStorage;
import org.andstatus.todoagenda.task.AbstractTaskProvider;
import org.andstatus.todoagenda.task.TaskEvent;

import java.util.ArrayList;
import java.util.List;

public class SamsungTasksProvider extends AbstractTaskProvider {

    public SamsungTasksProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    @Override
    public List<TaskEvent> getTasks() {
        initialiseParameters();
        return queryTasks();
    }

    private List<TaskEvent> queryTasks() {
        Uri uri = SamsungTasksContract.PROVIDER_URI;
        String[] projection = {
                SamsungTasksContract.COLUMN_ID,
                SamsungTasksContract.COLUMN_TITLE,
                SamsungTasksContract.COLUMN_DUE_DATE
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
        whereBuilder.append(SamsungTasksContract.COLUMN_COMPLETE).append(EQUALS).append("0");
        whereBuilder.append(AND).append(SamsungTasksContract.COLUMN_DELETED).append(EQUALS).append("0");

        whereBuilder.append(AND_BRACKET)
                .append(SamsungTasksContract.COLUMN_DUE_DATE).append(LTE).append(mEndOfTimeRange.getMillis())
                .append(OR)
                .append(SamsungTasksContract.COLUMN_DUE_DATE).append(IS_NULL)
                .append(CLOSING_BRACKET);

        return whereBuilder.toString();
    }

    private TaskEvent createTask(Cursor cursor) {
        TaskEvent task = new SamsungTaskEvent();
        task.setId(cursor.getLong(cursor.getColumnIndex(SamsungTasksContract.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(SamsungTasksContract.COLUMN_TITLE)));

        int dueDateIdx = cursor.getColumnIndex(SamsungTasksContract.COLUMN_DUE_DATE);
        Long dueMillis = null;
        if (!cursor.isNull(dueDateIdx)) {
            dueMillis = cursor.getLong(dueDateIdx);
        }
        task.setStartDate(getDueDate(dueMillis));

        return task;
    }
}
