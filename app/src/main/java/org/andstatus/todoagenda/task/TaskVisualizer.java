package org.andstatus.todoagenda.task;

import android.view.View;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.widget.EventEntryLayout;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;

import java.util.ArrayList;
import java.util.List;

import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setMultiline;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setViewWidth;

public class TaskVisualizer extends WidgetEntryVisualizer<TaskEntry> {
    private final AbstractTaskProvider eventProvider;

    public TaskVisualizer(EventProvider eventProvider) {
        super(eventProvider);
        this.eventProvider = (AbstractTaskProvider) eventProvider;
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        if (!(eventEntry instanceof TaskEntry)) return null;

        TaskEntry entry = (TaskEntry) eventEntry;
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), R.layout.task_entry);
        setColor(entry, rv);
        setDaysToEvent(entry, rv);
        setTitle(entry, rv);
        rv.setOnClickFillInIntent(R.id.event_entry, eventProvider.createViewEventIntent(entry.getEvent()));
        return rv;
    }

    private void setColor(TaskEntry entry, RemoteViews rv) {
        if (getSettings().getShowEventIcon()) {
            rv.setViewVisibility(R.id.event_entry_icon, View.VISIBLE);
            rv.setTextColor(R.id.event_entry_icon, entry.getEvent().getColor());
        } else {
            rv.setViewVisibility(R.id.event_entry_icon, View.GONE);
        }
        setBackgroundColor(rv, R.id.event_entry, getSettings().getEntryBackgroundColor(entry));
    }

    private void setDaysToEvent(TaskEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT) {
            rv.setViewVisibility(R.id.event_entry_days, View.GONE);
            rv.setViewVisibility(R.id.event_entry_days_right, View.GONE);
            rv.setViewVisibility(R.id.event_entry_time, View.GONE);
        } else {
            if (getSettings().getEntryDateFormat().type == DateFormatType.HIDDEN) {
                rv.setViewVisibility(R.id.event_entry_days, View.GONE);
                rv.setViewVisibility(R.id.event_entry_days_right, View.GONE);
            } else {
                int days = entry.getDaysToEvent();
                boolean daysAsText = getSettings().getEntryDateFormat().type != DateFormatType.NUMBER_OF_DAYS ||
                        days > -2 && days < 2;
                int viewToShow = daysAsText ? R.id.event_entry_days : R.id.event_entry_days_right;
                int viewToHide = daysAsText ? R.id.event_entry_days_right : R.id.event_entry_days;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                setViewWidth(getSettings(), rv, viewToShow, daysAsText
                        ? R.dimen.days_to_event_width
                        : R.dimen.days_to_event_right_width);
                rv.setTextViewText(viewToShow, entry.formatEntryDate());
                setTextSize(getSettings(), rv, viewToShow, R.dimen.event_entry_details);
                setTextColorFromAttr(getSettings().getShadingContext(TextShadingPref.forDetails(entry)),
                        rv, viewToShow, R.attr.dayHeaderTitle);
            }
            setViewWidth(getSettings(), rv, R.id.event_entry_time, R.dimen.event_time_width);
            rv.setViewVisibility(R.id.event_entry_time, View.VISIBLE);
        }
    }

    private void setTitle(TaskEntry entry, RemoteViews rv) {
        int viewId = R.id.event_entry_title;
        rv.setTextViewText(viewId, entry.getTitle());
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_title);
        setTextColorFromAttr(getSettings().getShadingContext(TextShadingPref.forTitle(entry)),
                rv, viewId, R.attr.eventEntryTitle);
        setMultiline(rv, viewId, getSettings().isMultilineTitle());
    }

    @Override
    public int getViewTypeCount() {
        return 1;
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
