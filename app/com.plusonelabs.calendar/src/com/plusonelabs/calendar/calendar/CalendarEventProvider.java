package com.plusonelabs.calendar.calendar;

import static com.plusonelabs.calendar.prefs.ICalendarPreferences.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.CalendarIntentUtil;
import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.IEventProvider;
import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.model.EventEntry;

public class CalendarEventProvider implements IEventProvider<CalendarEntry> {

	private static final String TWELVE = "12";
	private static final String AUTO = "auto";
	private static final String SPACE_ARROW = " →";
	private static final String ARROW_SPACE = "→ ";
	private static final String EMPTY_STRING = "";
	private static final String TIME_FORMAT_24 = "HH:mm";
	private static final String TIME_FORMAT_12 = "h:mm aa";
	private static final String SPACED_DASH = " - ";
	private static final String METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";

	private SimpleDateFormat timeFormatter12 = new SimpleDateFormat(TIME_FORMAT_12);
	private SimpleDateFormat timeFormatter24 = new SimpleDateFormat(TIME_FORMAT_24);

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
		rv.setOnClickFillInIntent(R.id.event_entry, createOnItemClickIntent(event));
		String title = event.getTitle();
		if (title.equals(EMPTY_STRING)) {
			title = context.getResources().getString(R.string.no_title);
		}
		rv.setTextViewText(R.id.event_entry_title, title);
		if (event.isAllDay() || event.spansFullDay()) {
			rv.setViewVisibility(R.id.event_entry_date, View.GONE);
		} else {
			rv.setViewVisibility(R.id.event_entry_date, View.VISIBLE);
			rv.setTextViewText(R.id.event_entry_date, createTimeSpanString(event));
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

	public Intent createOnItemClickIntent(CalendarEntry event) {
		CalendarEntry originalEvent = event.getOriginalEvent();
		if (originalEvent != null) {
			event = originalEvent;
		}
		return CalendarIntentUtil.createOpenCalendarEventIntent(event.getEventId(),
				event.getStartDate(), event.getEndDate());
	}

	public String createTimeSpanString(CalendarEntry event) {
		String startStr = null;
		String endStr = null;
		String separator = SPACED_DASH;
		if (event.isPartOfMultiDayEvent() && DateUtil.isMidnight(event.getStartDate())) {
			startStr = ARROW_SPACE;
			separator = EMPTY_STRING;
		} else {
			startStr = createTimeString(event.getStartDate());
		}
		if (event.isPartOfMultiDayEvent() && DateUtil.isMidnight(event.getEndDate())) {
			endStr = SPACE_ARROW;
			separator = EMPTY_STRING;
		} else {
			endStr = createTimeString(event.getEndDate());
		}
		return startStr + separator + endStr;
	}

	public String createTimeString(long time) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String dateFormat = prefs.getString(PREF_DATE_FORMAT, PREF_DATE_FORMAT_DEFAULT);
		if (DateUtil.hasAmPmClock(Locale.getDefault()) && dateFormat.equals(AUTO)
				|| dateFormat.equals(TWELVE)) {
			return timeFormatter12.format(new Date(time)).toLowerCase();
		}
		return timeFormatter24.format(new Date(time));
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

	public Class<? extends CalendarEntry> getSupportedEventEntryType() {
		return CalendarEntry.class;
	}

}
