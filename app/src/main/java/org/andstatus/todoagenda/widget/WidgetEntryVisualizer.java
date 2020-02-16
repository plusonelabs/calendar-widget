package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.AlarmIndicatorScaled;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.RecurringIndicatorScaled;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.util.MyStringBuilder;
import org.andstatus.todoagenda.util.RemoteViewsUtil;

import java.util.List;

import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setMultiline;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setViewWidth;
import static org.andstatus.todoagenda.widget.EventEntryLayout.SPACE_PIPE_SPACE;

public abstract class WidgetEntryVisualizer<T extends WidgetEntry<T>> {
    private final EventProvider eventProvider;

    public WidgetEntryVisualizer(EventProvider eventProvider) {
        this.eventProvider = eventProvider;
    }

    public RemoteViews getRemoteViews(WidgetEntry entry, int position) {

        RemoteViews rv = new RemoteViews(getContext().getPackageName(), getSettings().getEventEntryLayout().layoutId);
        setTitle(entry, rv);
        setDetails(entry, rv);
        setDate(entry, rv);
        setTime(entry, rv);

        setIndicators(entry, rv);
        setBackgroundColor(rv, R.id.event_entry, getSettings().getEntryBackgroundColor(entry));
        return rv;
    }

    protected void setIndicators(WidgetEntry entry, RemoteViews rv) {
        for (AlarmIndicatorScaled indicator : AlarmIndicatorScaled.values()) {
            rv.setViewVisibility(indicator.indicatorResId, View.GONE);
        }
        for (RecurringIndicatorScaled indicator : RecurringIndicatorScaled.values()) {
            rv.setViewVisibility(indicator.indicatorResId, View.GONE);
        }
    }

    @NonNull
    protected InstanceSettings getSettings() {
        return eventProvider.getSettings();
    }

    public Context getContext() {
        return eventProvider.context;
    }

    public abstract List<T> queryEventEntries();

    protected void setTitle(WidgetEntry entry, RemoteViews rv) {
        int viewId = R.id.event_entry_title;
        rv.setTextViewText(viewId, getTitleString(entry));
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_title);
        setTextColorFromAttr(getSettings().getShadingContext(TextShadingPref.forTitle(entry)),
                rv, viewId, R.attr.eventEntryTitle);
        setMultiline(rv, viewId, getSettings().isMultilineTitle());
    }

    protected CharSequence getTitleString(WidgetEntry event) {
        return getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT
            ? event.getTitle()
            : MyStringBuilder.of(event.getTitle())
                .withSeparator(event.getLocationString(), SPACE_PIPE_SPACE);
    }

    protected void setDetails(WidgetEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.ONE_LINE) return;

        MyStringBuilder eventDetails = MyStringBuilder
                .of(entry.formatEntryDate())
                .withSpace(entry.getEventTimeString())
                .withSeparator(entry.getLocationString(), SPACE_PIPE_SPACE);
        int viewId = R.id.event_entry_details;
        if (eventDetails.isEmpty()) {
            rv.setViewVisibility(viewId, View.GONE);
        } else {
            rv.setViewVisibility(viewId, View.VISIBLE);
            rv.setTextViewText(viewId, eventDetails);
            setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_details);
            setTextColorFromAttr(getSettings().getShadingContext(TextShadingPref.forDetails(entry)),
                    rv, viewId, R.attr.dayHeaderTitle);
            setMultiline(rv, viewId, getSettings().isMultilineDetails());
        }
    }

    protected void setDate(WidgetEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT) return;

        if (getSettings().getEntryDateFormat().type == DateFormatType.HIDDEN) {
            rv.setViewVisibility(R.id.event_entry_days, View.GONE);
            rv.setViewVisibility(R.id.event_entry_days_right, View.GONE);
        } else {
            int days = getSettings().clock().getNumberOfDaysTo(entry.entryDate);
            boolean daysAsText = getSettings().getEntryDateFormat().type != DateFormatType.NUMBER_OF_DAYS ||
                    (days > -2 && days < 2);

            int viewToShow = daysAsText ? R.id.event_entry_days : R.id.event_entry_days_right;
            int viewToHide = daysAsText ? R.id.event_entry_days_right : R.id.event_entry_days;
            rv.setViewVisibility(viewToHide, View.GONE);
            rv.setViewVisibility(viewToShow, View.VISIBLE);

            rv.setTextViewText(viewToShow, entry.formatEntryDate());
            setViewWidth(getSettings(), rv, viewToShow, daysAsText
                    ? R.dimen.days_to_event_width
                    : R.dimen.days_to_event_right_width);
            setTextSize(getSettings(), rv, viewToShow, R.dimen.event_entry_details);
            setTextColorFromAttr(getSettings().getShadingContext(TextShadingPref.forDetails(entry)),
                    rv, viewToShow, R.attr.dayHeaderTitle);
        }
    }

    protected void setTime(WidgetEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT) return;

        int viewId = R.id.event_entry_time;
        RemoteViewsUtil.setMultiline(rv, viewId, getSettings().getShowEndTime());
        rv.setTextViewText(viewId, entry.getEventTimeString().replace(CalendarEntry
                .SPACE_DASH_SPACE, "\n"));
        setViewWidth(getSettings(), rv, viewId, R.dimen.event_time_width);
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_details);
        setTextColorFromAttr(getSettings().getShadingContext(TextShadingPref.forDetails(entry)),
                rv, viewId, R.attr.dayHeaderTitle);
    }

}