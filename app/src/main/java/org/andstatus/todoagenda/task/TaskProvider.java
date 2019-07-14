package org.andstatus.todoagenda.task;

import android.app.Activity;
import android.content.Context;

import org.andstatus.todoagenda.EventProvider;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;
import org.andstatus.todoagenda.task.samsung.SamsungTasksProvider;

import java.util.List;

public class TaskProvider extends EventProvider {

    private static final String PROVIDER_DMFS = "DMFS_OPEN_TASKS";
    private static final String PROVIDER_SAMSUNG = "SAMSUNG";

    public TaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    public List<TaskEvent> getEvents() {
        AbstractTaskProvider provider = getProvider();
        return provider.getTasks();
    }

    public boolean hasPermission() {
        AbstractTaskProvider provider = getProvider();
        return provider.hasPermission();
    }

    public void requestPermission(Activity activity) {
        AbstractTaskProvider provider = getProvider();
        provider.requestPermission(activity);
    }

    private AbstractTaskProvider getProvider() {
        String taskSource = getSettings().getTaskSource();
        if (PROVIDER_DMFS.equals(taskSource)) {
            return new DmfsOpenTasksProvider(context, widgetId);
        }
        if (PROVIDER_SAMSUNG.equals(taskSource)) {
            return new SamsungTasksProvider(context, widgetId);
        }

        return new EmptyTaskProvider(context, widgetId);
    }
}
