package com.plusonelabs.calendar.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.RemoteViewsUtil;
import com.plusonelabs.calendar.prefs.CalendarPreferences;

import static com.plusonelabs.calendar.RemoteViewsUtil.setMultiline;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextColorFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextSize;

/**
 * @author yvolk@yurivolkov.com
 */
public enum EventEntryLayout {
    DEFAULT(R.layout.event_entry, "DEFAULT", R.string.default_multiline_layout) {

        @Override
        protected void setEventDetails(Context context, CalendarEntry entry, RemoteViews rv) {
            String eventDetails = entry.getEventTimeString(context) + entry.getLocationString(context);
            if (TextUtils.isEmpty(eventDetails)) {
                rv.setViewVisibility(R.id.event_entry_details, View.GONE);
            } else {
                rv.setViewVisibility(R.id.event_entry_details, View.VISIBLE);
                rv.setTextViewText(R.id.event_entry_details, eventDetails);
                setTextSize(context, rv, R.id.event_entry_details, R.dimen.event_entry_details);
                setTextColorFromAttr(context, rv, R.id.event_entry_details, R.attr.eventEntryDetails);
            }
        }
    },
    ONE_LINE(R.layout.event_entry_one_line, "ONE_LINE", R.string.single_line_layout) {

        @Override
        protected String getTitleString(Context context, CalendarEntry event) {
            return event.getTitle(context) + event.getLocationString(context);
        }

        @Override
        protected void setEventDate(Context context, CalendarEntry entry, RemoteViews rv) {
            if (CalendarPreferences.getShowDayHeaders(context)) {
                rv.setViewVisibility(R.id.event_entry_date, View.GONE);
            } else {
                rv.setViewVisibility(R.id.event_entry_date, View.VISIBLE);
                rv.setTextViewText(R.id.event_entry_date, entry.getDateString(context));
            }
        }

        @Override
        protected void setEventTime(Context context, CalendarEntry entry, RemoteViews rv) {
            RemoteViewsUtil.setMultiline(rv, R.id.event_entry_time, CalendarPreferences.getShowEndTime(context));
            rv.setTextViewText(R.id.event_entry_time, entry.getEventTimeString(context).replace(CalendarEntry
                    .SPACE_DASH_SPACE, " "));
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

    public void visualizeEvent(Context context, CalendarEntry entry, RemoteViews rv) {
        setTitle(context, entry, rv);
        setEventDate(context, entry, rv);
        setEventTime(context, entry, rv);
        setEventDetails(context, entry, rv);
    }

    protected void setTitle(Context context, CalendarEntry event, RemoteViews rv) {
        rv.setTextViewText(R.id.event_entry_title, getTitleString(context, event));
        setTextSize(context, rv, R.id.event_entry_title, R.dimen.event_entry_title);
        setTextColorFromAttr(context, rv, R.id.event_entry_title, R.attr.eventEntryTitle);
        setMultiline(rv, R.id.event_entry_title, CalendarPreferences.isTitleMultiline(context));
    }

    protected String getTitleString(Context context, CalendarEntry event) {
        return event.getTitle(context);
    }

    protected void setEventDate(Context context, CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventTime(Context context, CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventDetails(Context context, CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

}
