package com.plusonelabs.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.plusonelabs.calendar.calendar.CalendarEventVisualizer;
import com.plusonelabs.calendar.model.DayHeader;
import com.plusonelabs.calendar.model.Event;
import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static com.plusonelabs.calendar.RemoteViewsUtil.setBackgroundColor;
import static com.plusonelabs.calendar.RemoteViewsUtil.setBackgroundColorFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setPadding;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextColorFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextSize;
import static com.plusonelabs.calendar.Theme.getCurrentThemeId;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_DAYS_WITHOUT_EVENTS;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_DAY_HEADER_ALIGNMENT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_DAY_HEADER_ALIGNMENT_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME_DEFAULT;

public class EventRemoteViewsFactory implements RemoteViewsFactory {

	private final Context context;
	private volatile List<Event> mEventEntries = new ArrayList<>();
	private final List<IEventVisualizer<?>> eventProviders;

	public EventRemoteViewsFactory(Context context) {
		this.context = context;
        eventProviders = new ArrayList<>();
        eventProviders.add(new CalendarEventVisualizer(context));
    }

    public void onCreate() {
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(context));
	}

	public void onDestroy() {
		// Empty
	}

	public int getCount() {
		return mEventEntries.size();
	}

	public RemoteViews getViewAt(int position) {
        List<Event> eventEntries = mEventEntries;
		if (position < eventEntries.size()) {
			Event entry = eventEntries.get(position);
			if (entry instanceof DayHeader) {
				return updateDayHeader((DayHeader) entry);
			}
            for (IEventVisualizer<?> eventProvider : eventProviders) {
                if (entry.getClass().isAssignableFrom(eventProvider.getSupportedEventEntryType())) {
                    return eventProvider.getRemoteView(entry);
                }
            }
		}
		return null;
	}

	public RemoteViews updateDayHeader(DayHeader dayHeader) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String alignment = prefs.getString(PREF_DAY_HEADER_ALIGNMENT, PREF_DAY_HEADER_ALIGNMENT_DEFAULT);
        RemoteViews rv = new RemoteViews(context.getPackageName(), Alignment.valueOf(alignment).getLayoutId());
        String dateString = DateUtil.createDateString(context, dayHeader.getStartDate())
                .toUpperCase(Locale.getDefault());
        rv.setTextViewText(R.id.day_header_title, dateString);
        setTextSize(context, rv, R.id.day_header_title, R.dimen.day_header_title);
        setTextColorFromAttr(context, rv, R.id.day_header_title, R.attr.dayHeaderTitle);
        setBackgroundColor(rv, R.id.day_header,
                dayHeader.getStartDay().plusDays(1).isBeforeNow() ? CalendarPreferences.getPastEventsBackgroundColor(context) : Color.TRANSPARENT);
        setBackgroundColorFromAttr(context, rv, R.id.day_header_separator, R.attr.dayHeaderSeparator);
        setPadding(context, rv, R.id.day_header_title, 0, R.dimen.day_header_padding_top, R.dimen.day_header_padding_right, R.dimen.day_header_padding_bottom);
		Intent intent = createOpenCalendarAtDayIntent(dayHeader.getStartDate());
		rv.setOnClickFillInIntent(R.id.day_header, intent);
		return rv;
	}

    public void onDataSetChanged() {
        context.setTheme(getCurrentThemeId(context, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT));
        List<Event> events = new ArrayList<>();
        for (IEventVisualizer<?> eventProvider : eventProviders) {
            events.addAll(eventProvider.getEventEntries());
        }
        updateEntryList(events);
    }

    private void updateEntryList(List<Event> eventList) {
        List<Event> eventEntries = new ArrayList<>();
        if (!eventList.isEmpty()) {
            boolean showDaysWithoutEvents = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS, false);
            DayHeader curDayBucket = new DayHeader(new DateTime(0));
            for (Event event : eventList) {
                DateTime nextStartOfDay = event.getStartDay();
                if (!nextStartOfDay.isEqual(curDayBucket.getStartDay())) {
                    if (showDaysWithoutEvents) {
                        addEmptyDayHeadersBetweenTwoDays(eventEntries, curDayBucket.getStartDay(), nextStartOfDay);
                    }
                    curDayBucket = new DayHeader(nextStartOfDay);
                    eventEntries.add(curDayBucket);
                }
                eventEntries.add(event);
            }
            Log.v("Event list", eventList.toString());
            Log.v("Event entries", eventEntries.toString());
        }
        mEventEntries = eventEntries;
}

    private void addEmptyDayHeadersBetweenTwoDays(List<Event> eventEntries, DateTime fromDayExclusive, DateTime toDayExclusive) {
        DateTime emptyDay = fromDayExclusive.plusDays(1);
        DateTime today = DateTime.now().withTimeAtStartOfDay();
        if (emptyDay.isBefore(today)) {
            emptyDay = today;
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            eventEntries.add(new DayHeader(emptyDay));
            emptyDay = emptyDay.plusDays(1);
        }
    }

	public RemoteViews getLoadingView() {
        return null;
	}

	public int getViewTypeCount() {
		int result = 3; // we have 3 because of the "left", "right" and "center" day headers
        for (IEventVisualizer<?> eventProvider : eventProviders) {
            result += eventProvider.getViewTypeCount();
        }
		return result;
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean hasStableIds() {
		return true;
	}

}
