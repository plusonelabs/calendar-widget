package com.plusonelabs.calendar.calendar;

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

import com.plusonelabs.calendar.CalendarIntentUtil;
import com.plusonelabs.calendar.IEventProvider;
import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.model.EventEntry;

public class CalendarEventProvider implements IEventProvider<CalendarEntry> {

	private static final String TIME_FORMAT = "HH:mm";
	private static final String METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";
	private static final String SPACED_DASH = " - ";

	private SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);

	private final Context context;
	private CalendarContentProvider calendarContentProvider;
	private SharedPreferences prefs;

	public CalendarEventProvider(Context context) {
		this.context = context;
		calendarContentProvider = new CalendarContentProvider(context);
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public RemoteViews getRemoteView(EventEntry eventEntry) {
		CalendarEntry event = (CalendarEntry) eventEntry;
		RemoteViews rv = new RemoteViews(context.getPackageName(), getEventEntryLayout());
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

	public String createTimeString(long time) {
		return timeFormatter.format(new Date(time));
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

	public int getViewTypeCount() {
		return 6;
	}

	public ArrayList<CalendarEntry> getEventEntries() {
		return calendarContentProvider.getEvents();
	}

	public Class<? extends CalendarEntry> getSupportedEventEntryTypes() {
		return CalendarEntry.class;
	}

}
