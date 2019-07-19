package org.andstatus.todoagenda.task;

import android.content.Context;

import org.andstatus.todoagenda.DateUtil;
import org.andstatus.todoagenda.EventProvider;
import org.andstatus.todoagenda.prefs.EventSource;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

public abstract class AbstractTaskProvider extends EventProvider {

    protected DateTime now;

    public AbstractTaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        now = DateUtil.now(zone);
    }

    public abstract List<TaskEvent> getTasks();

    public abstract Collection<EventSource> getTaskLists();
}
