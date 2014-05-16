package com.plusonelabs.calendar.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;

import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_EVENT_RANGE;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_EVENT_RANGE_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_FILL_ALL_DAY;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_FILL_ALL_DAY_DEFAULT;

public class CalendarEventProvider {

	private static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC ";
	private static final String EVENT_SELECTION = Instances.SELF_ATTENDEE_STATUS + "!="
			+ Attendees.ATTENDEE_STATUS_DECLINED;
    private static final String[] PROJECTION_4_0 = new String[]{Instances.EVENT_ID, Instances.TITLE,
            Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.EVENT_LOCATION,
            Instances.HAS_ALARM, Instances.RRULE, Instances.CALENDAR_COLOR, Instances.EVENT_COLOR};
    private static final String[] PROJECTION_4_1 = new String[]{Instances.EVENT_ID, Instances.TITLE,
            Instances.BEGIN, Instances.END, Instances.ALL_DAY, Instances.EVENT_LOCATION,
            Instances.HAS_ALARM, Instances.RRULE, Instances.DISPLAY_COLOR};
    private static final String CLOSING_BRACKET = " )";
    private static final String OR = " OR ";
	private static final String EQUALS = " = ";
	private static final String AND_BRACKET = " AND (";

	private final Context context;

	public CalendarEventProvider(Context context) {
		this.context = context;
	}

	public List<CalendarEvent> getEvents() {
		Cursor cursor = createLoadedCursor();
		if (cursor != null) {
			List<CalendarEvent> eventList = createEventList(cursor);
			cursor.close();
			Collections.sort(eventList);
			return eventList;
		}
        return new ArrayList<>();
    }

	private List<CalendarEvent> createEventList(Cursor calendarCursor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fillAllDayEvents = prefs.getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT);
        List<CalendarEvent> eventList = new ArrayList<>();
        for (int i = 0; i < calendarCursor.getCount(); i++) {
			calendarCursor.moveToPosition(i);
			CalendarEvent event = createCalendarEvent(calendarCursor);
			setupDayOneEntry(eventList, event);
            if (!event.isAllDay() || fillAllDayEvents) {
                createFollowingEntries(eventList, event);
            }
        }
        return eventList;
    }

	public void setupDayOneEntry(List<CalendarEvent> eventList, CalendarEvent event) {
		if (isEqualOrAfterTodayAtMidnight(event.getStartDate())) {
			if (event.daysSpanned() > 1) {
				CalendarEvent clone = event.clone();
                clone.setEndDate(event.getStartDate().plusDays(1).withTimeAtStartOfDay());
                clone.setSpansMultipleDays(true);
                clone.setOriginalEvent(event);
                eventList.add(clone);
            } else {
				eventList.add(event);
			}
		}
	}

	public void createFollowingEntries(List<CalendarEvent> eventList, CalendarEvent event) {
		int daysCovered = event.daysSpanned();
		for (int j = 1; j < daysCovered; j++) {
            DateTime startDate = event.getStartDate().withTimeAtStartOfDay().plusDays(j);
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

	private boolean isEqualOrAfterTodayAtMidnight(DateTime startDate) {
        DateTime startOfDay = DateTime.now().withTimeAtStartOfDay();
        return startDate.isEqual(startOfDay) || startDate.isAfter(startOfDay);
    }

    private CalendarEvent cloneAsSpanningEvent(CalendarEvent eventEntry, DateTime startDate,
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
		
		// #51: No conversion anymore for all-day time stamps since they're passed via CalendarIntent and may not differ from original DB values.
		// All-day events begin and end at 0:00, are TZ independent and thus can internally be mapped to local time zone.
		// see also: http://developer.android.com/reference/android/provider/CalendarContract.Events.html
		event.setStartDate(new DateTime(calendarCursor.getLong(2)));            
		event.setEndDate(new DateTime(calendarCursor.getLong(3)));
		                
		event.setAllDay(calendarCursor.getInt(4) > 0);
		event.setLocation(calendarCursor.getString(5));
		event.setAlarmActive(calendarCursor.getInt(6) > 0);
		event.setRecurring(calendarCursor.getString(7) != null);
		event.setColor(getAsOpaque(getEventColor(calendarCursor)));
		return event;
	}

    private int getEventColor(Cursor calendarCursor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return calendarCursor.getInt(8);
        } else {
            int eventColor = calendarCursor.getInt(9);
            if (eventColor > 0) {
                return eventColor;
            }
            return calendarCursor.getInt(8);
        }
    }

    private int getAsOpaque(int color) {
        return argb(255, red(color), green(color), blue(color));
	}

	private Cursor createLoadedCursor() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int dateRange = Integer
				.valueOf(prefs.getString(PREF_EVENT_RANGE, PREF_EVENT_RANGE_DEFAULT));
		long start = System.currentTimeMillis();
		long end = start + DateUtils.DAY_IN_MILLIS * dateRange;
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, start);
		ContentUris.appendId(builder, end);
		String selection = createSelectionClause();
		ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(builder.build(), getProjection(), selection, null, EVENT_SORT_ORDER);
    }

    private String[] getProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return PROJECTION_4_1;
        }
        return PROJECTION_4_0;
    }

    private String createSelectionClause() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> activeCalenders = prefs.getStringSet(CalendarPreferences.PREF_ACTIVE_CALENDARS,
				new HashSet<String>());
		if (activeCalenders.isEmpty()) {
			return EVENT_SELECTION;
		}
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AND_BRACKET);
        Iterator<String> iterator = activeCalenders.iterator();
		while (iterator.hasNext()) {
			String calendarId = iterator.next();
            stringBuilder.append(Instances.CALENDAR_ID);
            stringBuilder.append(EQUALS);
            stringBuilder.append(calendarId);
            if (iterator.hasNext()) {
                stringBuilder.append(OR);
            }
		}
        stringBuilder.append(CLOSING_BRACKET);
        return EVENT_SELECTION + stringBuilder.toString();
    }
}
 
