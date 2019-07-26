package org.andstatus.todoagenda.task;

import android.content.Context;

import org.andstatus.todoagenda.EventProvider;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;
import org.andstatus.todoagenda.task.samsung.SamsungTasksProvider;

import java.util.Collection;
import java.util.List;

public class TaskProvider extends EventProvider {

    public static final String PROVIDER_NONE = "NONE";

    private static final String PROVIDER_DMFS = "DMFS_OPEN_TASKS";
    private static final String PROVIDER_SAMSUNG = "SAMSUNG";

    public TaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    public List<TaskEvent> getEvents() {
        AbstractTaskProvider provider = getProvider();
        return provider.getTasks();
    }

    // This is called from the settings activity, when the task source that the user
    // selected has not been saved to settings yet
    public Collection<EventSource> getTaskLists(String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.getTaskLists();
    }

    private AbstractTaskProvider getProvider() {
        String taskSource = getSettings().getTaskSource();
        return getProvider(taskSource);
    }

    private AbstractTaskProvider getProvider(String taskSource) {
        if (PROVIDER_DMFS.equals(taskSource)) {
            return new DmfsOpenTasksProvider(context, widgetId);
        }
        if (PROVIDER_SAMSUNG.equals(taskSource)) {
            return new SamsungTasksProvider(context, widgetId);
        }

        return new EmptyTaskProvider(context, widgetId);
    }
}
