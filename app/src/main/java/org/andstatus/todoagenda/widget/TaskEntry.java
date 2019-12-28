package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.task.TaskEvent;
import org.joda.time.DateTime;

import static org.andstatus.todoagenda.widget.WidgetEntryPosition.ENTRY_DATE;

public class TaskEntry extends WidgetEntry<TaskEntry> {
    private TaskEvent event;

    public static TaskEntry fromEvent(TaskEvent event) {
        TaskEntry entry = new TaskEntry(ENTRY_DATE, event.getStartDate(), event.getDueDate());
        entry.event = event;
        return entry;
    }

    private TaskEntry(WidgetEntryPosition entryPosition, DateTime entryDate, DateTime endDate) {
        super(entryPosition, entryDate, endDate);
    }

    @Override
    public OrderedEventSource getSource() {
        return event.getEventSource();
    }

    @Override
    public String getTitle() {
        return event.getTitle();
    }

    public TaskEvent getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return super.toString() + " TaskEntry [startDate=" + event.getStartDate() + ", dueDate=" + event.getDueDate() +
                "]";
    }
}
