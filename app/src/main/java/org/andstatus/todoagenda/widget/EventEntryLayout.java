package org.andstatus.todoagenda.widget;

import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.RemoteViewsUtil;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import static org.andstatus.todoagenda.util.RemoteViewsUtil.setMultiline;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setViewWidth;

/**
 * @author yvolk@yurivolkov.com
 */
public enum EventEntryLayout {
    DEFAULT(R.layout.event_entry, "DEFAULT", R.string.default_multiline_layout) {
        @Override
        protected void setEventDetails(CalendarEntry entry, RemoteViews rv) {
            String eventDetails = entry.getEventTimeString() + entry.getLocationString();
            int viewId = R.id.event_entry_details;
            if (TextUtils.isEmpty(eventDetails)) {
                rv.setViewVisibility(viewId, View.GONE);
            } else {
                rv.setViewVisibility(viewId, View.VISIBLE);
                rv.setTextViewText(viewId, eventDetails);
                setTextSize(entry.getSettings(), rv, viewId, R.dimen.event_entry_details);
                setTextColorFromAttr(entry.getSettings().getShadingContext(TextShadingPref.getDayHeader(entry)),
                        rv, viewId, R.attr.dayHeaderTitle);
            }
        }
    },
    ONE_LINE(R.layout.event_entry_one_line, "ONE_LINE", R.string.single_line_layout) {
        @Override
        protected String getTitleString(CalendarEntry event) {
            return event.getTitle() + event.getLocationString();
        }

        @Override
        protected void setDaysToEvent(CalendarEntry entry, RemoteViews rv) {
            if (entry.getSettings().getShowNumberOfDaysToEvent()) {
                int days = entry.getDaysFromToday();
                boolean daysAsText = days > -2 && days < 2;
                int viewToShow = daysAsText ? R.id.event_entry_days : R.id.event_entry_days_right;
                int viewToHide = daysAsText ? R.id.event_entry_days_right : R.id.event_entry_days;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                rv.setTextViewText(viewToShow, DateUtil.getDaysFromTodayString(entry.getContext(), days));
                InstanceSettings settings = entry.getSettings();
                setViewWidth(settings, rv, viewToShow, daysAsText
                        ? R.dimen.days_to_event_width
                        : R.dimen.days_to_event_right_width);
                setTextSize(settings, rv, viewToShow, R.dimen.event_entry_details);
                setTextColorFromAttr(settings.getShadingContext(TextShadingPref.getDayHeader(entry)),
                        rv, viewToShow, R.attr.dayHeaderTitle);
            } else {
                rv.setViewVisibility(R.id.event_entry_days, View.GONE);
                rv.setViewVisibility(R.id.event_entry_days_right, View.GONE);
            }
        }

        @Override
        protected void setEventTime(CalendarEntry entry, RemoteViews rv) {
            int viewId = R.id.event_entry_time;
            RemoteViewsUtil.setMultiline(rv, viewId, entry.getSettings().getShowEndTime());
            rv.setTextViewText(viewId, entry.getEventTimeString().replace(CalendarEntry
                    .SPACE_DASH_SPACE, "\n"));
            InstanceSettings settings = entry.getSettings();
            setViewWidth(settings, rv, viewId, R.dimen.event_time_width);
            setTextSize(settings, rv, viewId, R.dimen.event_entry_details);
            setTextColorFromAttr(settings.getShadingContext(TextShadingPref.getDayHeader(entry)),
                    rv, viewId, R.attr.dayHeaderTitle);
        }
    };

    @LayoutRes
    public final int layoutId;
    public final String value;
    @StringRes
    public final int summaryResId;

    EventEntryLayout(@LayoutRes int layoutId, String value, int summaryResId) {
        this.layoutId = layoutId;
        this.value = value;
        this.summaryResId = summaryResId;
    }

    public static EventEntryLayout fromValue(String value) {
        EventEntryLayout layout = DEFAULT;
        for (EventEntryLayout item : EventEntryLayout.values()) {
            if (item.value.equals(value)) {
                layout = item;
                break;
            }
        }
        return layout;
    }

    public void visualizeEvent(CalendarEntry entry, RemoteViews rv) {
        setTitle(entry, rv);
        setDaysToEvent(entry, rv);
        setEventTime(entry, rv);
        setEventDetails(entry, rv);
    }

    protected void setTitle(CalendarEntry entry, RemoteViews rv) {
        int viewId = R.id.event_entry_title;
        rv.setTextViewText(viewId, getTitleString(entry));
        setTextSize(entry.getSettings(), rv, viewId, R.dimen.event_entry_title);
        setTextColorFromAttr(entry.getSettings().getShadingContext(TextShadingPref.getEntry(entry)),
                rv, viewId, R.attr.eventEntryTitle);
        setMultiline(rv, viewId, entry.getSettings().isTitleMultiline());
    }

    protected String getTitleString(CalendarEntry event) {
        return event.getTitle();
    }

    protected void setDaysToEvent(CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventTime(CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventDetails(CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

}
