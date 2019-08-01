package org.andstatus.todoagenda.task;

import android.content.Context;
import android.content.Intent;

import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.provider.EventProvider;
import org.joda.time.DateTime;

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

    public abstract List<TaskEvent> getEvents();

    public abstract Intent createViewEventIntent(TaskEvent event);
}
