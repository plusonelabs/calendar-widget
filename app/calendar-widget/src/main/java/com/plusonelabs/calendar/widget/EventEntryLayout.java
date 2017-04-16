package com.plusonelabs.calendar.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.RemoteViewsUtil;

import static com.plusonelabs.calendar.RemoteViewsUtil.*;

/**
 * @author yvolk@yurivolkov.com
 */
public enum EventEntryLayout {
    DEFAULT(R.layout.event_entry, "DEFAULT", R.string.default_multiline_layout) {
        @Override
        protected void setEventDetails(CalendarEntry entry, RemoteViews rv) {
            String eventDetails = entry.getEventTimeString() + entry.getLocationString();
            if (TextUtils.isEmpty(eventDetails)) {
                rv.setViewVisibility(R.id.event_entry_details, View.GONE);
            } else {
                rv.setViewVisibility(R.id.event_entry_details, View.VISIBLE);
                rv.setTextViewText(R.id.event_entry_details, eventDetails);
                setTextSize(entry.getSettings(), rv, R.id.event_entry_details, R.dimen.event_entry_details);
                setTextColorFromAttr(entry.getSettings().getEntryThemeContext(), rv, R.id.event_entry_details,
                        R.attr.eventEntryDetails);
            }
        }
    },
    ONE_LINE(R.layout.event_entry_one_line, "ONE_LINE", R.string.single_line_layout) {
        @Override
        protected String getTitleString(CalendarEntry event) {
            return event.getTitle() + event.getLocationString();
        }

        @Override
        protected void setEventDate(CalendarEntry entry, RemoteViews rv) {
            if (entry.getSettings().getShowDayHeaders()) {
                rv.setViewVisibility(R.id.event_entry_date, View.GONE);
                rv.setViewVisibility(R.id.event_entry_date_right, View.GONE);
            } else {
                int days = entry.getDaysFromToday();
                int viewToShow = days < -1 || days > 1 ? R.id.event_entry_date_right : R.id.event_entry_date;
                int viewToHide = viewToShow == R.id.event_entry_date ? R.id.event_entry_date_right : R.id.event_entry_date;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                rv.setTextViewText(viewToShow, getDaysFromTodayString(entry.getSettings().getEntryThemeContext(), days));
            }
        }

        private CharSequence getDaysFromTodayString(Context context, int daysFromToday) {
            switch (daysFromToday) {
                case -1:
                    return context.getText(R.string.yesterday);
                case 0:
                    return context.getText(R.string.today);
                case 1:
                    return context.getText(R.string.tomorrow);
                default:
                    return Integer.toString(daysFromToday);
            }
        }

        @Override
        protected void setEventTime(CalendarEntry entry, RemoteViews rv) {
            RemoteViewsUtil.setMultiline(rv, R.id.event_entry_time, entry.getSettings().getShowEndTime());
            rv.setTextViewText(R.id.event_entry_time, entry.getEventTimeString().replace(CalendarEntry
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

    public void visualizeEvent(CalendarEntry entry, RemoteViews rv) {
        setTitle(entry, rv);
        setEventDate(entry, rv);
        setEventTime(entry, rv);
        setEventDetails(entry, rv);
    }

    protected void setTitle(CalendarEntry event, RemoteViews rv) {
        rv.setTextViewText(R.id.event_entry_title, getTitleString(event));
        setTextSize(event.getSettings(), rv, R.id.event_entry_title, R.dimen.event_entry_title);
        setTextColorFromAttr(event.getSettings().getEntryThemeContext(), rv, R.id.event_entry_title, R.attr.eventEntryTitle);
        setMultiline(rv, R.id.event_entry_title, event.getSettings().isTitleMultiline());
    }

    protected String getTitleString(CalendarEntry event) {
        return event.getTitle();
    }

    protected void setEventDate(CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventTime(CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventDetails(CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

}
