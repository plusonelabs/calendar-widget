package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.prefs.TaskScheduling;
import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.util.MyClock;
import org.joda.time.DateTime;

import static org.andstatus.todoagenda.widget.WidgetEntryPosition.END_OF_LIST;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.ENTRY_DATE;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.PAST_AND_DUE;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.START_OF_TODAY;

public class TaskEntry extends WidgetEntry<TaskEntry> {
    private TaskEvent event;

    public static TaskEntry fromEvent(InstanceSettings settings, TaskEvent event) {
        WidgetEntryPosition entryPosition = getEntryPosition(settings, event);
        TaskEntry entry = new TaskEntry(settings, entryPosition, getEntryDate(settings, entryPosition, event), event.getDueDate());
        entry.event = event;
        return entry;
    }

    private TaskEntry(InstanceSettings settings, WidgetEntryPosition entryPosition, DateTime entryDate, DateTime endDate) {
        super(settings, entryPosition, entryDate, endDate);
    }

    /** See https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887 **/
    private static WidgetEntryPosition getEntryPosition(InstanceSettings settings, TaskEvent event) {
        if (!event.hasStartDate() && !event.hasDueDate()) return settings.getTaskWithoutDates().widgetEntryPosition;

        if (event.hasDueDate() && settings.clock().isBeforeToday(event.getDueDate())) {
            return settings.getShowPastEventsUnderOneHeader() ? PAST_AND_DUE : ENTRY_DATE;
        }

        DateTime mainDate = mainDate(settings, event);
        if (mainDate != null) {
            if (mainDate.isAfter(settings.getEndOfTimeRange())) return END_OF_LIST;
        }

        DateTime otherDate = otherDate(settings, event);

        if (settings.getTaskScheduling() == TaskScheduling.DATE_DUE) {
            if (!event.hasDueDate()) {
                if (settings.clock().isBeforeToday(event.getStartDate())) return START_OF_TODAY;
                if (event.getStartDate().isAfter(settings.getEndOfTimeRange())) return END_OF_LIST;
            }
        } else {
            if (!event.hasStartDate() || settings.clock().isBeforeToday(event.getStartDate())) {
                    return START_OF_TODAY;
            }
        }
        return WidgetEntry.getEntryPosition(settings, mainDate, otherDate);
    }

    private static DateTime mainDate(InstanceSettings settings, TaskEvent event) {
        return settings.getTaskScheduling() == TaskScheduling.DATE_DUE
                ? event.getDueDate()
                : event.getStartDate();
    }

    private static DateTime otherDate(InstanceSettings settings, TaskEvent event) {
        return settings.getTaskScheduling() == TaskScheduling.DATE_DUE
                ? event.getStartDate()
                : event.getDueDate();
    }

    private static DateTime getEntryDate(InstanceSettings settings, WidgetEntryPosition entryPosition, TaskEvent event) {
        switch (entryPosition) {
            case END_OF_TODAY:
            case END_OF_LIST:
            case END_OF_LIST_HEADER:
            case LIST_FOOTER:
            case HIDDEN:
                return getEntryDateOrElse(settings, event, MyClock.DATETIME_MAX);
            default:
                return getEntryDateOrElse(settings, event, MyClock.DATETIME_MIN);
        }
    }

    private static DateTime getEntryDateOrElse(InstanceSettings settings, TaskEvent event, DateTime defaultDate) {
        if (settings.getTaskScheduling() == TaskScheduling.DATE_DUE) {
            return event.hasDueDate()
                    ? event.getDueDate()
                    : (event.hasStartDate() ? event.getStartDate() : defaultDate);
        } else {
            if (event.hasStartDate()) {
                if (settings.clock().isBeforeToday(event.getStartDate())) {
                    return event.hasDueDate() ? event.getDueDate() : defaultDate;
                }
                return event.getStartDate();
            } else {
                return event.hasDueDate() ? event.getDueDate() : defaultDate;
            }
        }
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
        return super.toString() + " TaskEntry [title='" + event.getTitle() + "', startDate=" + event.getStartDate() +
                ", dueDate=" + event.getDueDate() + "]";
    }
}
