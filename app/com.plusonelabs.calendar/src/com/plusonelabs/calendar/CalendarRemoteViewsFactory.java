package com.plusonelabs.calendar;

import static com.plusonelabs.calendar.prefs.ICalendarPreferences.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.plusonelabs.calendar.model.CalenderEntry;
import com.plusonelabs.calendar.model.DayHeader;
import com.plusonelabs.calendar.model.EventEntry;

public class CalendarRemoteViewsFactory implements RemoteViewsFactory {

	private static final String TIME_FORMAT = "HH:mm";
	private static final String DAY_STRING_FORMAT = "EEEE, ";
	private static final String DAY_DATE_FORMAT = "dd. MMMM";

	private static final String METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";

	private static final String SPACED_DASH = " - ";
	private static final String COMMA_SPACE = ", ";

	static SimpleDateFormat dayDateFormatter;
	static SimpleDateFormat dayStringFormatter;
	static SimpleDateFormat timeFormatter;

	private final Context context;
	private SharedPreferences prefs;
	private ArrayList<CalenderEntry> calenderEntries;
	private CalendarEventProvider calendarEventProvider;

	static {
		initiDateFormatter();
	}

	public CalendarRemoteViewsFactory(Context context) {
		this.context = context;
		calendarEventProvider = new CalendarEventProvider(context);
		calenderEntries = new ArrayList<CalenderEntry>();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void onCreate() {
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		rv.setPendingIntentTemplate(R.id.event_list,
				CalendarIntentUtil.createOpenCalendarEventPendingIntent(context));
	}

	public void onDestroy() {
		calenderEntries.clear();
	}

	public int getCount() {
		return calenderEntries.size();
	}

	public RemoteViews getViewAt(int position) {
		String packageName = context.getPackageName();
		CalenderEntry entry = calenderEntries.get(position);
		if (entry instanceof DayHeader) {
			return updateDayHeader(packageName, (DayHeader) entry);
		} else if (entry instanceof EventEntry) {
			return updateEventEntry(packageName, (EventEntry) entry);
		}
		return null;
	}

	public static void initiDateFormatter() {
		dayDateFormatter = new SimpleDateFormat(DAY_DATE_FORMAT);
		dayStringFormatter = new SimpleDateFormat(DAY_STRING_FORMAT);
		timeFormatter = new SimpleDateFormat(TIME_FORMAT);
	}

	public RemoteViews updateEventEntry(String packageName, EventEntry event) {
		RemoteViews rv = new RemoteViews(packageName, getEventEntryLayout());
		Intent intent = CalendarIntentUtil.createOpenCalendarEventIntent(event.getEventId(),
				event.getStartDate(), event.getEndDate());
		rv.setOnClickFillInIntent(R.id.event_entry_text_layout, intent);
		rv.setOnClickFillInIntent(R.id.event_entry_color, intent);
		rv.setOnClickFillInIntent(R.id.event_entry_title, intent);
		rv.setTextViewText(R.id.event_entry_title, event.getTitle());
		if (event.isAllDay()) {
			rv.setViewVisibility(R.id.event_entry_date, View.GONE);
		} else {
			rv.setViewVisibility(R.id.event_entry_date, View.VISIBLE);
			rv.setOnClickFillInIntent(R.id.event_entry_date, intent);
			rv.setTextViewText(R.id.event_entry_date, createTimeString(event.getStartDate())
					+ SPACED_DASH + createTimeString(event.getEndDate()));
		}
		if (event.isAlarmActive() && prefs.getBoolean(PREF_INDICATE_ALERTS, true)) {
			rv.setViewVisibility(R.id.event_entry_indicator_alarm, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.event_entry_indicator_alarm, View.GONE);
		}
		if (event.isRecurring() && prefs.getBoolean(PREF_INDICATE_RECURRING, false)) {
			rv.setViewVisibility(R.id.event_entry_indicator_recurring, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.event_entry_indicator_recurring, View.GONE);
		}
		rv.setInt(R.id.event_entry_color, METHOD_SET_BACKGROUND_COLOR, event.getColor());
		return rv;
	}

	public RemoteViews updateDayHeader(String packageName, DayHeader dayHeader) {
		RemoteViews rv = new RemoteViews(packageName, getDayHeaderLayout());
		rv.setTextViewText(R.id.day_header_title, createDayEntryString(dayHeader));
		Intent intent = CalendarIntentUtil.createOpenCalendarAtDayIntent(context,
				dayHeader.getStartDate());
		rv.setOnClickFillInIntent(R.id.day_header_title, intent);
		rv.setOnClickFillInIntent(R.id.day_header, intent);
		return rv;
	}

	private int getEventEntryLayout() {
		String textSize = prefs.getString(PREF_TEXT_SIZE, PREF_TEXT_SIZE_MEDIUM);
		if (textSize.equals(PREF_TEXT_SIZE_SMALL)) {
			return R.layout.event_entry_small;
		} else if (textSize.equals(PREF_TEXT_SIZE_LARGE)) {
			return R.layout.event_entry_large;
		}
		return R.layout.event_entry_medium;
	}

	private int getDayHeaderLayout() {
		String textSize = prefs.getString(PREF_TEXT_SIZE, PREF_TEXT_SIZE_MEDIUM);
		if (textSize.equals(PREF_TEXT_SIZE_SMALL)) {
			return R.layout.day_header_small;
		} else if (textSize.equals(PREF_TEXT_SIZE_LARGE)) {
			return R.layout.day_header_large;
		}
		return R.layout.day_header_medium;
	}

	public String createTimeString(long time) {
		return timeFormatter.format(new Date(time));
	}

	public String createDayEntryString(DayHeader dayEntry) {
		long date = dayEntry.getStartDate();
		String prefix = "";
		if (dayEntry.isToday()) {
			prefix = context.getString(R.string.today) + COMMA_SPACE;
		} else if (dayEntry.isTomorrow()) {
			prefix = context.getString(R.string.tomorrow) + COMMA_SPACE;
		} else {
			prefix = dayStringFormatter.format(new Date(date)).toUpperCase();
		}
		return prefix + dayDateFormatter.format(new Date(date)).toUpperCase();
	}

	public void onDataSetChanged() {
		calenderEntries.clear();
		ArrayList<EventEntry> eventList = calendarEventProvider.getEventList();
		updateEntryList(eventList);
	}

	public void updateEntryList(ArrayList<EventEntry> eventList) {
		if (!eventList.isEmpty()) {
			DayHeader curDayBucket = new DayHeader(eventList.get(0).getStartDate());
			calenderEntries.add(curDayBucket);
			for (EventEntry event : eventList) {
				if (!event.isSameDay(curDayBucket.getStartDate())) {
					curDayBucket = new DayHeader(event.getStartDate());
					calenderEntries.add(curDayBucket);
				}
				calenderEntries.add(event);
			}
		}
	}

	public RemoteViews getLoadingView() {
		return null;
	}

	public int getViewTypeCount() {
		return 6;
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean hasStableIds() {
		return true;
	}

}
