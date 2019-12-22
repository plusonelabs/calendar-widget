package org.andstatus.todoagenda.task;

import android.content.Context;
import android.content.Intent;

import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
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

        now = DateUtil.now(zone);
    }

    List<TaskEvent> queryEvents() {
        initialiseParameters();
        if (PermissionsUtil.isPermissionNeeded(context, type.permission) ||
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

            // TODO: other checks...
        }

        return !mKeywordsFilter.matched(task.getTitle());
    }

    public abstract Intent createViewEventIntent(TaskEvent event);
}
