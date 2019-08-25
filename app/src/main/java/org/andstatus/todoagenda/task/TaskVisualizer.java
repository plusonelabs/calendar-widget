package org.andstatus.todoagenda.task;

import android.view.View;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.util.DateUtil;
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
    public RemoteViews getRemoteView(WidgetEntry eventEntry) {
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
        if (entry.getEvent().getDueDate().isBefore(DateUtil.now(entry.getEvent().getDueDate().getZone()))) {
            setBackgroundColor(rv, R.id.event_entry, getSettings().getPastEventsBackgroundColor());
        } else {
            setBackgroundColor(rv, R.id.event_entry, 0);
        }
    }

    private void setDaysToEvent(TaskEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT) {
            rv.setViewVisibility(R.id.task_one_line_days, View.GONE);
            rv.setViewVisibility(R.id.task_one_line_days_right, View.GONE);
            rv.setViewVisibility(R.id.task_one_line_spacer, View.GONE);
        } else {
            if (getSettings().getShowNumberOfDaysToEvent()) {
                int days = entry.getDaysFromToday();
                int viewToShow = days < -1 || days > 1 ? R.id.task_one_line_days_right : R.id.task_one_line_days;
                int viewToHide = viewToShow == R.id.task_one_line_days ? R.id.task_one_line_days_right : R.id.task_one_line_days;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                setViewWidth(getSettings(), rv, viewToShow, R.dimen.days_to_event_width);
                rv.setTextViewText(viewToShow, DateUtil.getDaysFromTodayString(getSettings().getContext(), days));
                setTextSize(getSettings(), rv, viewToShow, R.dimen.event_entry_details);
                setTextColorFromAttr(getSettings().getDayHeaderThemeContext(), rv, viewToShow, R.attr.dayHeaderTitle);
            } else {
                rv.setViewVisibility(R.id.task_one_line_days, View.GONE);
                rv.setViewVisibility(R.id.task_one_line_days_right, View.GONE);
            }
            setViewWidth(getSettings(), rv, R.id.task_one_line_spacer, R.dimen.days_to_event_width);
            rv.setViewVisibility(R.id.task_one_line_spacer, View.VISIBLE);
        }
    }

    private void setTitle(TaskEntry entry, RemoteViews rv) {
        int viewId = R.id.event_entry_title;
        rv.setTextViewText(viewId, entry.getTitle());
        if (getSettings().getShowEventIcon()) {
            setTextSize(getSettings(), rv, R.id.event_entry_icon, R.dimen.event_entry_title);
        }
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_title);
        setTextColorFromAttr(getSettings().getEntryThemeContext(), rv, viewId, R.attr.eventEntryTitle);
        setMultiline(rv, viewId, getSettings().isTitleMultiline());
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public List<TaskEntry> getEventEntries() {
        return createEntryList(eventProvider.getEvents());
    }

    private List<TaskEntry> createEntryList(List<TaskEvent> events) {
        List<TaskEntry> entries = new ArrayList<>();
        for (TaskEvent event : events) {
            entries.add(TaskEntry.fromEvent(event));
        }
        return entries;
    }

}
