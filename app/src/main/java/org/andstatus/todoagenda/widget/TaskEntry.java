package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.task.TaskEvent;

public class TaskEntry extends WidgetEntry {
    private TaskEvent event;

    public static TaskEntry fromEvent(TaskEvent event) {
        TaskEntry entry = new TaskEntry();
        entry.event = event;
        entry.setStartDate(event.getStartDate());
        return entry;
    }

    public String getTitle() {
        return event.getTitle();
    }

    public TaskEvent getEvent() {
        return event;
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
