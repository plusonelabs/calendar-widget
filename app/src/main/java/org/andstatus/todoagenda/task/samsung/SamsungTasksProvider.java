package org.andstatus.todoagenda.task.samsung;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.task.AbstractTaskProvider;
import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.util.CalendarIntentUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vavr.control.Try;

public class SamsungTasksProvider extends AbstractTaskProvider {
    private static final String TAG = SamsungTasksProvider.class.getSimpleName();

    public SamsungTasksProvider(EventProviderType type, Context context, int widgetId) {
        super(type, context, widgetId);
    }

    @Override
    public List<TaskEvent> queryTasks() {
        myContentResolver.onQueryEvents();

        Uri uri = SamsungTasksContract.Tasks.PROVIDER_URI;
        String[] projection = {
                SamsungTasksContract.Tasks.COLUMN_ID,
                SamsungTasksContract.Tasks.COLUMN_TITLE,
                SamsungTasksContract.Tasks.COLUMN_DUE_DATE,
                SamsungTasksContract.Tasks.COLUMN_COLOR,
                SamsungTasksContract.Tasks.COLUMN_LIST_ID,
        };
        String where = getWhereClause();

        return myContentResolver.foldEvents(uri, projection, where, null, null,
                new ArrayList<>(), tasks -> cursor -> {
                    TaskEvent task = createTask(cursor);
                    if (matchedFilter(task)) {
                        tasks.add(task);
                    }
                    return tasks;
                });
    }

    private String getWhereClause() {
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(SamsungTasksContract.Tasks.COLUMN_DELETED).append(EQUALS).append("0");

        if (getFilterMode() == FilterMode.NORMAL_FILTER) {
            whereBuilder.append(AND).append(SamsungTasksContract.Tasks.COLUMN_COMPLETE).append(EQUALS).append("0");
        }

        Set<String> taskLists = new HashSet<>();
        for (OrderedEventSource orderedSource: getSettings().getActiveEventSources(type)) {
            taskLists.add(Integer.toString(orderedSource.source.getId()));
        }
        if (!taskLists.isEmpty()) {
            whereBuilder.append(AND);
            whereBuilder.append(SamsungTasksContract.Tasks.COLUMN_LIST_ID);
            whereBuilder.append(" IN ( ");
            whereBuilder.append(TextUtils.join(",", taskLists));
            whereBuilder.append(CLOSING_BRACKET);
        }

        return whereBuilder.toString();
    }

    private TaskEvent createTask(Cursor cursor) {
        OrderedEventSource source = getSettings()
                .getActiveEventSource(type,
                        cursor.getInt(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_LIST_ID)));
        TaskEvent task = new TaskEvent(getSettings(), getSettings().clock().getZone());
        task.setEventSource(source);
        task.setId(cursor.getLong(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_TITLE)));

        int dueDateIdx = cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_DUE_DATE);
        Long dueMillis = null;
        if (!cursor.isNull(dueDateIdx)) {
            dueMillis = cursor.getLong(dueDateIdx);
        }
        task.setDates(null, dueMillis);

        task.setColor(getColor(cursor, cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_COLOR),
                cursor.getInt(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_LIST_ID))));

        return task;
    }

    @Override
    public Try<List<EventSource>> fetchAvailableSources() {
        String[] projection = {
                SamsungTasksContract.TaskLists.COLUMN_ID,
                SamsungTasksContract.TaskLists.COLUMN_NAME,
                SamsungTasksContract.TaskLists.COLUMN_COLOR,
        };
        String taskListName = context.getResources().getString(R.string.task_source_samsung);
        return myContentResolver.foldAvailableSources(
                SamsungTasksContract.TaskLists.PROVIDER_URI,
                projection,
                new ArrayList<>(),
                eventSources -> cursor -> {
                    int indId = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_ID);
                    int indSummary = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_NAME);
                    int indColor = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_COLOR);
                    int id = cursor.getInt(indId);
                    EventSource source = new EventSource(type, id, taskListName,
                            cursor.getString(indSummary), getColor(cursor, indColor, id), true);
                    eventSources.add(source);
                    return eventSources;
                });
    }

    @Override
    public Intent createViewEventIntent(TaskEvent event) {
        Intent intent = CalendarIntentUtil.createViewIntent();
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getId()));
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_TASK, true);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_SELECTED, event.getId());
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_ACTION_VIEW_FOCUS, 0);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_DETAIL_MODE, true);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_LAUNCH_FROM_WIDGET, true);
        return intent;
    }

    private int getColor(Cursor cursor, int colorIdx, int accountId) {
        if (!cursor.isNull(colorIdx)) {
            return getAsOpaque(cursor.getInt(colorIdx));
        } else {
            int[] fixedColors = context.getResources().getIntArray(R.array.task_list_colors);
            int arrayIdx = accountId % fixedColors.length;
            return fixedColors[arrayIdx];
        }
    }
}
