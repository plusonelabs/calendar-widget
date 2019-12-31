package org.andstatus.todoagenda.widget;

import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.task.TaskEvent;
import org.joda.time.DateTime;

import static org.andstatus.todoagenda.widget.WidgetEntryPosition.ENTRY_DATE;

public class TaskEntry extends WidgetEntry<TaskEntry> {
    private TaskEvent event;

    public static TaskEntry fromEvent(InstanceSettings settings, TaskEvent event) {
        TaskEntry entry = new TaskEntry(settings, ENTRY_DATE, event.getStartDate());
        entry.event = event;
        return entry;
    }

    private TaskEntry(InstanceSettings settings, WidgetEntryPosition entryPosition, DateTime entryDate) {
        super(settings, entryPosition, entryDate);
    }

    @Nullable
    @Override
    public DateTime getEndDate() {
        return event.getDueDate();
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
