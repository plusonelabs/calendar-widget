package org.andstatus.todoagenda.task;

import android.content.Context;

import org.andstatus.todoagenda.DateUtil;
import org.andstatus.todoagenda.EventProvider;
import org.joda.time.DateTime;

import java.util.List;

public abstract class AbstractTaskProvider extends EventProvider {

    protected DateTime now;

    public AbstractTaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        // Move endOfTime to end of day to include all tasks in the last day of range
        mEndOfTimeRange = mEndOfTimeRange.millisOfDay().withMaximumValue();

        now = DateUtil.now(zone);
    }

    public abstract List<TaskEvent> getTasks();

    protected DateTime getDueDate(Long dueMillis) {
        if (dueMillis == null) {
            return now.withTimeAtStartOfDay();
        } else {
            DateTime dueDate = new DateTime(dueMillis, zone);
            if (dueDate.isBefore(now)) {
                dueDate = now;
            }

            return dueDate.withTimeAtStartOfDay();
        }
    }
}
