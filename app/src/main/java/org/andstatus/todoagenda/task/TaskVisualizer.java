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

import static org.andstatus.todoagenda.util.RemoteViewsUtil.setMultiline;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

public class TaskVisualizer extends WidgetEntryVisualizer<TaskEntry> {
    private final AbstractTaskProvider eventProvider;

    public TaskVisualizer(EventProvider eventProvider) {
        this.eventProvider = (AbstractTaskProvider) eventProvider;
    }

    @Override
    public RemoteViews getRemoteView(WidgetEntry eventEntry) {
        if (!(eventEntry instanceof TaskEntry)) return null;

        TaskEntry entry = (TaskEntry) eventEntry;
        RemoteViews rv = new RemoteViews(eventProvider.context.getPackageName(), R.layout.task_entry);
        setColor(entry, rv);
        setDaysToEvent(entry, rv);
        setTitle(entry, rv);
        rv.setOnClickFillInIntent(R.id.task_entry, eventProvider.createViewEventIntent(entry.getEvent()));
        return rv;
    }

    private void setColor(TaskEntry entry, RemoteViews rv) {
        rv.setTextColor(R.id.task_entry_icon, entry.getEvent().getColor());
    }

    private void setDaysToEvent(TaskEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT) {
            rv.setViewVisibility(R.id.task_one_line_days, View.GONE);
            rv.setViewVisibility(R.id.task_one_line_days_right, View.GONE);
            rv.setViewVisibility(R.id.task_one_line_spacer, View.GONE);
        } else {
            if (getSettings().getShowDayHeaders()) {
                rv.setViewVisibility(R.id.task_one_line_days, View.GONE);
                rv.setViewVisibility(R.id.task_one_line_days_right, View.GONE);
                rv.setViewVisibility(R.id.task_one_line_spacer, View.VISIBLE);
            } else {
                int days = entry.getDaysFromToday();
                int viewToShow = days < -1 || days > 1 ? R.id.task_one_line_days_right : R.id.task_one_line_days;
                int viewToHide = viewToShow == R.id.task_one_line_days ? R.id.task_one_line_days_right : R.id.task_one_line_days;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                rv.setTextViewText(viewToShow, DateUtil.getDaysFromTodayString(getSettings().getEntryThemeContext(), days));
                rv.setViewVisibility(R.id.task_one_line_spacer, View.VISIBLE);
            }
        }
    }

    private void setTitle(TaskEntry entry, RemoteViews rv) {
        rv.setTextViewText(R.id.task_entry_title, entry.getTitle());
        setTextSize(getSettings(), rv, R.id.task_entry_icon, R.dimen.event_entry_title);
        setTextSize(getSettings(), rv, R.id.task_entry_title, R.dimen.event_entry_title);
        setTextColorFromAttr(getSettings().getEntryThemeContext(), rv, R.id.task_entry_title, R.attr.eventEntryTitle);
        setMultiline(rv, R.id.task_entry_title, getSettings().isTitleMultiline());
    }

    public InstanceSettings getSettings() {
        return InstanceSettings.fromId(eventProvider.context, eventProvider.widgetId);
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
