package org.andstatus.todoagenda.task;

import android.view.View;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;

import java.util.ArrayList;
import java.util.List;

public class TaskVisualizer extends WidgetEntryVisualizer<TaskEntry> {
    private final AbstractTaskProvider eventProvider;

    public TaskVisualizer(EventProvider eventProvider) {
        super(eventProvider);
        this.eventProvider = (AbstractTaskProvider) eventProvider;
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        if (!(eventEntry instanceof TaskEntry)) return null;

        RemoteViews rv = super.getRemoteViews(eventEntry, position);

        TaskEntry entry = (TaskEntry) eventEntry;
        rv.setOnClickFillInIntent(R.id.event_entry, eventProvider.createViewEventIntent(entry.getEvent()));
        setIcon(entry, rv);
        return rv;
    }

    private void setIcon(TaskEntry entry, RemoteViews rv) {
        if (getSettings().getShowEventIcon()) {
            rv.setViewVisibility(R.id.event_entry_icon, View.VISIBLE);
            rv.setTextColor(R.id.event_entry_icon, entry.getEvent().getColor());
        } else {
            rv.setViewVisibility(R.id.event_entry_icon, View.GONE);
        }
        rv.setViewVisibility(R.id.event_entry_color, View.GONE);
    }

    @Override
    public List<TaskEntry> queryEventEntries() {
        return createEntryList(eventProvider.queryEvents());
    }

    private List<TaskEntry> createEntryList(List<TaskEvent> events) {
        List<TaskEntry> entries = new ArrayList<>();
        for (TaskEvent event : events) {
            entries.add(TaskEntry.fromEvent(getSettings(), event));
        }
        return entries;
    }

}
