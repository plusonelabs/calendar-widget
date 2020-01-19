package org.andstatus.todoagenda.task;

import android.content.Context;
import android.content.Intent;

import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.prefs.TaskScheduling;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public abstract class AbstractTaskProvider extends EventProvider {

    protected DateTime now;

    public AbstractTaskProvider(EventProviderType type, Context context, int widgetId) {
        super(type, context, widgetId);
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        now = getSettings().clock().now();
    }

    List<TaskEvent> queryEvents() {
        initialiseParameters();
        if (myContentResolver.isPermissionNeeded(context, type.permission) ||
                getSettings().getActiveEventSources(type).isEmpty()) {
            return Collections.emptyList();
        }

        return queryTasks();
    }

    public abstract List<TaskEvent> queryTasks();

    /**
     * @return true - include the event in the result
     */
    protected boolean matchedFilter(TaskEvent task) {
        if (getFilterMode() == FilterMode.NO_FILTERING) return true;

        if (getFilterMode() == FilterMode.DEBUG_FILTER) {
            if (task.getStatus() == TaskStatus.COMPLETED) return false;
            if (task.hasStartDate() && task.getStartDate().isAfter(getSettings().getEndOfTimeRange())) return false;
        }
        if (getSettings().getTaskScheduling() == TaskScheduling.DATE_DUE) {
            if (!task.hasStartDate()) {
                if (task.hasDueDate() && task.getDueDate().isAfter(getSettings().getEndOfTimeRange())) return false;
            }
        }

        return !mKeywordsFilter.matched(task.getTitle());
    }

    public abstract Intent createViewEventIntent(TaskEvent event);
}
