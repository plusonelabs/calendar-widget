package com.plusonelabs.calendar.calendar;

import static com.plusonelabs.calendar.RemoteViewsUtil.*;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.*;

import java.util.ArrayList;
import java.util.Locale;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.CalendarIntentUtil;
import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.IEventVisualizer;
import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.model.Event;

public class CalendarEventVisualizer implements IEventVisualizer<CalendarEvent> {

	private static final String TWELVE = "12";
	private static final String AUTO = "auto";
	private static final String SPACE_ARROW = " →";
	private static final String ARROW_SPACE = "→ ";
	private static final String EMPTY_STRING = "";
	private static final String SPACE_DASH_SPACE = " - ";
	private static final String SPACE_PIPE_SPACE = "  |  ";

	private final Context context;
	private final CalendarEventProvider calendarContentProvider;
	private final SharedPreferences prefs;

	public CalendarEventVisualizer(Context context) {
		this.context = context;
		calendarContentProvider = new CalendarEventProvider(context);
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public RemoteViews getRemoteView(Event eventEntry) {
		CalendarEvent event = (CalendarEvent) eventEntry;
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.event_entry);
		rv.setOnClickFillInIntent(R.id.event_entry, createOnItemClickIntent(event));
		setTitle(event, rv);
		setEventDetails(event, rv);
		setAlarmActive(event, rv);
		setRecurring(event, rv);
		setColor(event, rv);
		return rv;
	}

	private void setTitle(CalendarEvent event, RemoteViews rv) {
		String title = event.getTitle();
		if (title == null || title.equals(EMPTY_STRING)) {
			title = context.getResources().getString(R.string.no_title);
		}
		rv.setTextViewText(R.id.event_entry_title, title);
		setTextSize(context, rv, R.id.event_entry_title, R.dimen.event_entry_title);
		setTextColorRes(context, rv, R.id.event_entry_title, R.attr.eventEntryTitle);
		setSingleLine(rv, R.id.event_entry_title,
				!prefs.getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT));
	}

	private void setEventDetails(CalendarEvent event, RemoteViews rv) {
		if (event.spansOneFullDay()
				&& !(event.isStartOfMultiDayEvent() || event.isEndOfMultiDayEvent())
				|| event.isAllDay()) {
			rv.setViewVisibility(R.id.event_entry_details, View.GONE);
		} else {
			String eventDetails = createTimeSpanString(event);
			boolean showLocation = prefs.getBoolean(PREF_SHOW_LOCATION, PREF_SHOW_LOCATION_DEFAULT);
			if (showLocation && event.getLocation() != null && !event.getLocation().isEmpty()) {
				eventDetails += SPACE_PIPE_SPACE + event.getLocation();
			}
			rv.setViewVisibility(R.id.event_entry_details, View.VISIBLE);
			rv.setTextViewText(R.id.event_entry_details, eventDetails);
			setTextSize(context, rv, R.id.event_entry_details, R.dimen.event_entry_details);
			setTextColorRes(context, rv, R.id.event_entry_details, R.attr.eventEntryDetails);
		}
	}

	private void setAlarmActive(CalendarEvent event, RemoteViews rv) {
		if (event.isAlarmActive() && prefs.getBoolean(PREF_INDICATE_ALERTS, true)) {
			rv.setViewVisibility(R.id.event_entry_indicator_alarm, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.event_entry_indicator_alarm, View.GONE);
		}
	}

	private void setRecurring(CalendarEvent event, RemoteViews rv) {
		if (event.isRecurring() && prefs.getBoolean(PREF_INDICATE_RECURRING, false)) {
			rv.setViewVisibility(R.id.event_entry_indicator_recurring, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.event_entry_indicator_recurring, View.GONE);
		}
	}

	private void setColor(CalendarEvent event, RemoteViews rv) {
		setBackgroundColor(rv, R.id.event_entry_color, event.getColor());
	}

	public Intent createOnItemClickIntent(CalendarEvent event) {
		CalendarEvent originalEvent = event.getOriginalEvent();
		if (originalEvent != null) {
			event = originalEvent;
		}
		return CalendarIntentUtil.createOpenCalendarEventIntent(event.getEventId(),
				event.getStartDate(), event.getEndDate());
	}

	public String createTimeSpanString(CalendarEvent event) {
		String startStr = null;
		String endStr = null;
		String separator = SPACE_DASH_SPACE;

		if (event.isPartOfMultiDayEvent() && DateUtil.isMidnight(event.getStartDate())
				&& !event.isStartOfMultiDayEvent()) {
			startStr = ARROW_SPACE;
			separator = EMPTY_STRING;
		} else {
			startStr = createTimeString(event.getStartDate());
		}

		if (prefs.getBoolean(PREF_SHOW_END_TIME, PREF_SHOW_END_TIME_DEFAULT)) {
			if (event.isPartOfMultiDayEvent() && DateUtil.isMidnight(event.getEndDate())
					&& !event.isEndOfMultiDayEvent()) {
				endStr = SPACE_ARROW;
				separator = EMPTY_STRING;
			} else {
				endStr = createTimeString(event.getEndDate());
			}
		} else {
			separator = EMPTY_STRING;
			endStr = EMPTY_STRING;
		}
		return startStr + separator + endStr;
	}

	public String createTimeString(DateTime time) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String dateFormat = prefs.getString(PREF_DATE_FORMAT, PREF_DATE_FORMAT_DEFAULT);
		if (DateUtil.hasAmPmClock(Locale.getDefault()) && dateFormat.equals(AUTO)
				|| dateFormat.equals(TWELVE)) {
			return DateUtils.formatDateTime(context, time.toDate().getTime(),
					DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_12HOUR);
		}
		return DateUtils.formatDateTime(context, time.toDate().getTime(),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
	}

	public int getViewTypeCount() {
		return 2;
	}

	public ArrayList<CalendarEvent> getEventEntries() {
		return calendarContentProvider.getEvents();
	}

	public Class<? extends CalendarEvent> getSupportedEventEntryType() {
		return CalendarEvent.class;
	}

}
