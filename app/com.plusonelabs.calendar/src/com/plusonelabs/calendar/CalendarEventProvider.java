package com.plusonelabs.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

import com.plusonelabs.calendar.model.EventEntry;
import com.plusonelabs.calendar.prefs.ICalendarPreferences;

public class CalendarEventProvider {

	private static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC ";
	private static final String EVENT_SELECTION = Instances.SELF_ATTENDEE_STATUS + "!="
			+ Attendees.ATTENDEE_STATUS_DECLINED;
	private static final String[] PROJECTION = new String[] { Instances.EVENT_ID, Instances.TITLE,
			Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.CALENDAR_COLOR };

	private final Context context;

	public CalendarEventProvider(Context context) {
		this.context = context;
	}

	public ArrayList<EventEntry> getEventList() {
		Cursor cursor = createLoadedCursor();
		ArrayList<EventEntry> eventList = createEventList(cursor);
		cursor.close();
		Collections.sort(eventList);
		return eventList;
	}

	private ArrayList<EventEntry> createEventList(Cursor calendarCursor) {
		ArrayList<EventEntry> eventList = new ArrayList<EventEntry>();
		for (int i = 0; i < calendarCursor.getCount(); i++) {
			calendarCursor.moveToPosition(i);
			EventEntry eventEntry = new EventEntry();
			eventEntry.setEventId(calendarCursor.getInt(0));
			eventEntry.setTitle(calendarCursor.getString(1));
			eventEntry.setStartDate(calendarCursor.getLong(2));
			eventEntry.setEndDate(calendarCursor.getLong(3));
			eventEntry.setAllDay(calendarCursor.getInt(4) > 0);
			eventEntry.setColor(calendarCursor.getInt(5));
			eventList.add(eventEntry);
		}
		return eventList;
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

	public String createSelectionClause() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> activeCalenders = prefs.getStringSet(
				ICalendarPreferences.PREF_ACTIVE_CALENDARS, new HashSet<String>());
		if (activeCalenders.isEmpty()) {
			return EVENT_SELECTION;
		}
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" AND (");
		Iterator<String> iter = activeCalenders.iterator();
		while (iter.hasNext()) {
			String calendarId = iter.next();
			strBuf.append(Instances.CALENDAR_ID);
			strBuf.append(" = ");
			strBuf.append(calendarId);
			if (iter.hasNext()) {
				strBuf.append(" OR ");
			}
		}
		strBuf.append(" )");
		return EVENT_SELECTION + strBuf.toString();
	}
}
