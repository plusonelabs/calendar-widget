package com.plusonelabs.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.plusonelabs.calendar.calendar.CalendarEventVisualizer;
import com.plusonelabs.calendar.model.DayHeader;
import com.plusonelabs.calendar.model.Event;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.plusonelabs.calendar.Alignment.valueOf;
import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
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
	private final List<Event> eventEntries;
	private final List<IEventVisualizer<?>> eventProviders;

	public EventRemoteViewsFactory(Context context) {
		this.context = context;
        eventProviders = new ArrayList<>();
        eventProviders.add(new CalendarEventVisualizer(context));
        eventEntries = new ArrayList<>();
    }

    public void onCreate() {
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(context));
	}

	public void onDestroy() {
		eventEntries.clear();
	}

	public int getCount() {
		return eventEntries.size();
	}

	public RemoteViews getViewAt(int position) {
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
        RemoteViews rv = new RemoteViews(context.getPackageName(), valueOf(alignment).getLayoutId());
        String dateString = DateUtil.createDateString(context, dayHeader.getStartDate())
                .toUpperCase(Locale.getDefault());
        rv.setTextViewText(R.id.day_header_title, dateString);
        setTextSize(context, rv, R.id.day_header_title, R.dimen.day_header_title);
        setTextColorFromAttr(context, rv, R.id.day_header_title, R.attr.dayHeaderTitle);
        setBackgroundColorFromAttr(context, rv, R.id.day_header_separator, R.attr.dayHeaderSeparator);
        setPadding(context, rv, R.id.day_header_title, 0, R.dimen.day_header_padding_top,
                R.dimen.day_header_padding_right, R.dimen.day_header_padding_bottom);
		Intent intent = createOpenCalendarAtDayIntent(dayHeader.getStartDate());
		rv.setOnClickFillInIntent(R.id.day_header, intent);
		return rv;
	}

    public void onDataSetChanged() {
        context.setTheme(getCurrentThemeId(context, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT));
        eventEntries.clear();
        List<Event> events = new ArrayList<>();
        for (IEventVisualizer<?> eventProvider : eventProviders) {
            events.addAll(eventProvider.getEventEntries());
        }
        updateEntryList(events);
    }

    /**
     * Add empty day headers between (but not including) two dates.
     */
    private void addEmptyDayEntries(DateTime fromExclusive, DateTime toExclusive) {
        DateTime d0 = fromExclusive.withTimeAtStartOfDay();
        DateTime dEnd = toExclusive.withTimeAtStartOfDay();
        for (DateTime emptyDay = d0.plusDays(1);
             emptyDay.isBefore(dEnd);
             emptyDay = emptyDay.plusDays(1).withTimeAtStartOfDay()) {
            eventEntries.add(new DayHeader(emptyDay));
        }
    }

    private void updateEntryList(List<Event> eventList) {
        if (eventList.isEmpty()) {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showDaysWithoutEvents = prefs.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS, false);

        Event firstEvent = eventList.get(0);
        if (showDaysWithoutEvents) {
            // Add initial empty day buckets until first event
            addEmptyDayEntries(DateTime.now().minusDays(1), firstEvent.getStartDate());
        }

        DayHeader curDayBucket = new DayHeader(firstEvent.getStartDate());
        eventEntries.add(curDayBucket);
        for (Event event : eventList) {
            DateTime startDate = event.getStartDate();
            if (!startDate.withTimeAtStartOfDay().isEqual(
                    curDayBucket.getStartDate().withTimeAtStartOfDay())) {
                if (showDaysWithoutEvents) {
                    addEmptyDayEntries(curDayBucket.getStartDate(), startDate);
                }
                curDayBucket = new DayHeader(startDate);
                eventEntries.add(curDayBucket);
            }
            eventEntries.add(event);
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
