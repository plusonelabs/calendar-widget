package com.plusonelabs.calendar.calendar;

import static android.graphics.Color.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;

import com.plusonelabs.calendar.prefs.ICalendarPreferences;

public class CalendarEventProvider {

	private static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC ";
	private static final String EVENT_SELECTION = Instances.SELF_ATTENDEE_STATUS + "!="
			+ Attendees.ATTENDEE_STATUS_DECLINED;
	private static final String[] PROJECTION = new String[] { Instances.EVENT_ID, Instances.TITLE,
			Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.CALENDAR_COLOR,
			Instances.EVENT_COLOR, Instances.HAS_ALARM, Instances.RRULE };
	private static final String CLOSING_BRACKET = " )";
	private static final String OR = " OR ";
	private static final String EQUALS = " = ";
	private static final String AND_BRACKET = " AND (";

	private final Context context;

	public CalendarEventProvider(Context context) {
		this.context = context;
	}

	public ArrayList<CalendarEvent> getEvents() {
		Cursor cursor = createLoadedCursor();
		if (cursor != null) {
			ArrayList<CalendarEvent> eventList = createEventList(cursor);
			cursor.close();
			Collections.sort(eventList);
			return eventList;
		}
		return new ArrayList<CalendarEvent>();
	}

	private ArrayList<CalendarEvent> createEventList(Cursor calendarCursor) {
		ArrayList<CalendarEvent> eventList = new ArrayList<CalendarEvent>();
		for (int i = 0; i < calendarCursor.getCount(); i++) {
			calendarCursor.moveToPosition(i);
			CalendarEvent event = createCalendarEvent(calendarCursor);
			setupDayOneEntry(eventList, event);
			createFollowingEntries(eventList, event);
		}
		return eventList;
	}

	public void setupDayOneEntry(ArrayList<CalendarEvent> eventList, CalendarEvent event) {
		if (isEqualOrAfterTodayAtMidnight(event.getStartDate())) {
			if (event.daysSpanned() > 1) {
				CalendarEvent clone = event.clone();
				clone.setEndDate(event.getStartDate().plusDays(1).toDateMidnight().toDateTime());
				clone.setSpansMultipleDays(true);
				clone.setOriginalEvent(event);
				eventList.add(clone);
			} else {
				eventList.add(event);
			}
		}
	}

	public void createFollowingEntries(ArrayList<CalendarEvent> eventList, CalendarEvent event) {
		int daysCovered = event.daysSpanned();
		for (int j = 1; j < daysCovered; j++) {
			DateTime startDate = event.getStartDate().toDateMidnight().plusDays(j).toDateTime();
			if (isEqualOrAfterTodayAtMidnight(startDate)) {
				DateTime endDate;
				if (j < daysCovered - 1) {
					endDate = startDate.plusDays(1);
				} else {
					endDate = event.getEndDate();
				}
				eventList.add(cloneAsSpanningEvent(event, startDate, endDate));
			}
		}
	}

	public boolean isEqualOrAfterTodayAtMidnight(DateTime startDate) {
		DateMidnight midnight = new DateMidnight();
		return startDate.isEqual(midnight) || startDate.isAfter(midnight);
	}

	public CalendarEvent cloneAsSpanningEvent(CalendarEvent eventEntry, DateTime startDate,
			DateTime endDate) {
		CalendarEvent clone = eventEntry.clone();
		clone.setStartDate(startDate);
		clone.setEndDate(endDate);
		clone.setSpansMultipleDays(true);
		clone.setOriginalEvent(eventEntry);
		return clone;
	}

	private CalendarEvent createCalendarEvent(Cursor calendarCursor) {
		CalendarEvent event = new CalendarEvent();
		event.setEventId(calendarCursor.getInt(0));
		event.setTitle(calendarCursor.getString(1));
		event.setStartDate(new DateTime(calendarCursor.getLong(2)));
		event.setEndDate(new DateTime(calendarCursor.getLong(3)));
		event.setAllDay(calendarCursor.getInt(4) > 0);
		event.setColor(getAsOpaque(getEntryColor(calendarCursor)));
		event.setAlarmActive(calendarCursor.getInt(7) > 0);
		event.setRecurring(calendarCursor.getString(8) != null);
		if (event.isAllDay()) {
			DateTime startDate = event.getStartDate();
			long converted = startDate.getZone().convertLocalToUTC(startDate.getMillis(), true);
			event.setStartDate(new DateTime(converted));
			DateTime endDate = event.getEndDate();
			converted = endDate.getZone().convertLocalToUTC(endDate.getMillis(), true);
			event.setEndDate(new DateTime(converted));
		}
		return event;
	}

	public int getEntryColor(Cursor calendarCursor) {
		int eventColor = calendarCursor.getInt(6);
		if (eventColor > 0) {
			return eventColor;
		}
		return calendarCursor.getInt(5);
	}

	private int getAsOpaque(int color) {
		return argb(255, red(color), green(color), blue(color));
	}

	private Cursor createLoadedCursor() {
		long start = System.currentTimeMillis();
		long end = start + DateUtils.DAY_IN_MILLIS * 31;
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, start);
		ContentUris.appendId(builder, end);
		String selection = createSelectionClause();
		ContentResolver contentResolver = context.getContentResolver();
		return contentResolver
				.query(builder.build(), PROJECTION, selection, null, EVENT_SORT_ORDER);
	}

	private String createSelectionClause() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> activeCalenders = prefs.getStringSet(
				ICalendarPreferences.PREF_ACTIVE_CALENDARS, new HashSet<String>());
		if (activeCalenders.isEmpty()) {
			return EVENT_SELECTION;
		}
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(AND_BRACKET);
		Iterator<String> iter = activeCalenders.iterator();
		while (iter.hasNext()) {
			String calendarId = iter.next();
			strBuf.append(Instances.CALENDAR_ID);
			strBuf.append(EQUALS);
			strBuf.append(calendarId);
			if (iter.hasNext()) {
				strBuf.append(OR);
			}
		}
		strBuf.append(CLOSING_BRACKET);
		return EVENT_SELECTION + strBuf.toString();
	}
}
