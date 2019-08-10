package org.andstatus.todoagenda.task;

import android.content.Context;
import android.content.Intent;

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

    List<TaskEvent> getEvents() {
        initialiseParameters();
        if (PermissionsUtil.isPermissionNeeded(context, type.permission) ||
                getSettings().getActiveEventSources(type).isEmpty()) {
            return Collections.emptyList();
        }

        return queryTasks();
    }

    public abstract List<TaskEvent> queryTasks();

    public abstract Intent createViewEventIntent(TaskEvent event);
}
