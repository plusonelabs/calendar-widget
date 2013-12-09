package com.plusonelabs.calendar;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.plusonelabs.calendar.calendar.CalendarEventVisualizer;
import com.plusonelabs.calendar.model.DayHeader;
import com.plusonelabs.calendar.model.Event;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static com.plusonelabs.calendar.RemoteViewsUtil.setBackgroundColorRes;
import static com.plusonelabs.calendar.RemoteViewsUtil.setPadding;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextColorRes;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextSize;
import static com.plusonelabs.calendar.Theme.getCurrentThemeId;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME_DEFAULT;

public class EventRemoteViewsFactory implements RemoteViewsFactory {

	private static final String COMMA_SPACE = ", ";

	private final Context context;
	private final ArrayList<Event> eventEntries;

	private final ArrayList<IEventVisualizer<?>> eventProviders;

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
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_header);
		rv.setTextViewText(R.id.day_header_title, createDayEntryString(dayHeader));
		setTextSize(context, rv, R.id.day_header_title, R.dimen.day_header_title);
		setTextColorRes(context, rv, R.id.day_header_title, R.attr.dayHeaderTitle);
		setBackgroundColorRes(context, rv, R.id.day_header_separator, R.attr.dayHeaderSeparator);
		setPadding(context, rv, R.id.day_header_title, 0, R.dimen.day_header_padding_top,
				R.dimen.day_header_padding_right, R.dimen.day_header_padding_bottom);
		Intent intent = createOpenCalendarAtDayIntent(context, dayHeader.getStartDate());
		rv.setOnClickFillInIntent(R.id.day_header, intent);
		return rv;
	}

	public String createDayEntryString(DayHeader dayEntry) {
		Date date = dayEntry.getStartDate().toDate();
        if (dayEntry.isToday()) {
            return createDateString(date, context.getString(R.string.today));
        } else if (dayEntry.isTomorrow()) {
            return createDateString(date, context.getString(R.string.tomorrow));
        }
        return DateUtils.formatDateTime(context, date.getTime(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY).toUpperCase(
				Locale.getDefault());
	}

    private String createDateString(Date date, String text) {
        return text + COMMA_SPACE + DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_DATE)
                .toUpperCase(Locale.getDefault());
    }

    public void onDataSetChanged() {
        context.setTheme(getCurrentThemeId(context, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT));
        eventEntries.clear();
        ArrayList<Event> events = new ArrayList<>();
        for (IEventVisualizer<?> eventProvider : eventProviders) {
            events.addAll(eventProvider.getEventEntries());
        }
        updateEntryList(events);
    }

	private void updateEntryList(ArrayList<Event> eventList) {
		if (!eventList.isEmpty()) {
			Event firstEvent = eventList.get(0);
			DayHeader curDayBucket = new DayHeader(firstEvent.getStartDate());
			eventEntries.add(curDayBucket);
			for (Event event : eventList) {
				DateTime startDate = event.getStartDate();
				if (!startDate.toDateMidnight().isEqual(
						curDayBucket.getStartDate().toDateMidnight())) {
					curDayBucket = new DayHeader(startDate);
					eventEntries.add(curDayBucket);
				}
				eventEntries.add(event);
			}
		}
	}

	public RemoteViews getLoadingView() {
		return null;
	}

	public int getViewTypeCount() {
		int result = 0;
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
