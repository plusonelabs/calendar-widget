package com.plusonelabs.calendar.calendar;

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
import com.plusonelabs.calendar.prefs.CalendarPreferences;
import com.plusonelabs.calendar.widget.CalendarEntry;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.plusonelabs.calendar.RemoteViewsUtil.setAlpha;
import static com.plusonelabs.calendar.RemoteViewsUtil.setBackgroundColor;
import static com.plusonelabs.calendar.RemoteViewsUtil.setImageFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setSingleLine;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextColorFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextSize;
import static com.plusonelabs.calendar.Theme.getCurrentThemeId;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_DATE_FORMAT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_DATE_FORMAT_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_INDICATE_ALERTS;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_INDICATE_RECURRING;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_MULTILINE_TITLE;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_MULTILINE_TITLE_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_END_TIME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_END_TIME_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_LOCATION;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_LOCATION_DEFAULT;

public class CalendarEventVisualizer implements IEventVisualizer<CalendarEntry> {

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

	public RemoteViews getRemoteView(WidgetEntry eventEntry) {
		CalendarEntry event = (CalendarEntry) eventEntry;
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.event_entry);
		rv.setOnClickFillInIntent(R.id.event_entry, createOnItemClickIntent(event.getEvent()));
        setTitle(event, rv);
		setEventDetails(event, rv);
		setAlarmActive(event, rv);
        setRecurring(event, rv);
		setColor(event, rv);
		return rv;
	}

	private void setTitle(CalendarEntry event, RemoteViews rv) {
		String title = event.getTitle();
		if (title == null || title.equals(EMPTY_STRING)) {
			title = context.getResources().getString(R.string.no_title);
		}
		rv.setTextViewText(R.id.event_entry_title, title);
		setTextSize(context, rv, R.id.event_entry_title, R.dimen.event_entry_title);
        setTextColorFromAttr(context, rv, R.id.event_entry_title, R.attr.eventEntryTitle);
        setSingleLine(rv, R.id.event_entry_title,
                !prefs.getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT));
    }

	private void setEventDetails(CalendarEntry entry, RemoteViews rv) {
        boolean fillAllDayEvents = CalendarPreferences.getFillAllDayEvents(context);
        if (entry.spansOneFullDay() && !(entry.isStartOfMultiDayEvent()
                || entry.isEndOfMultiDayEvent())
                || entry.isAllDay() && fillAllDayEvents) {
            rv.setViewVisibility(R.id.event_entry_details, View.GONE);
        } else {
            String eventDetails = createTimeSpanString(entry);
            boolean showLocation = prefs.getBoolean(PREF_SHOW_LOCATION, PREF_SHOW_LOCATION_DEFAULT);
            if (showLocation && entry.getLocation() != null && !entry.getLocation().isEmpty()) {
                eventDetails += SPACE_PIPE_SPACE + entry.getLocation();
            }
            rv.setViewVisibility(R.id.event_entry_details, View.VISIBLE);
            rv.setTextViewText(R.id.event_entry_details, eventDetails);
            setTextSize(context, rv, R.id.event_entry_details, R.dimen.event_entry_details);
            setTextColorFromAttr(context, rv, R.id.event_entry_details, R.attr.eventEntryDetails);
        }
    }

    private void setAlarmActive(CalendarEntry entry, RemoteViews rv) {
        boolean showIndication = entry.isAlarmActive() && prefs.getBoolean(PREF_INDICATE_ALERTS, true);
        setIndicator(rv, showIndication, R.id.event_entry_indicator_alarm, R.attr.eventEntryAlarm);
	}

	private void setRecurring(CalendarEntry entry, RemoteViews rv) {
        boolean showIndication = entry.isRecurring() && prefs.getBoolean(PREF_INDICATE_RECURRING, false);
        setIndicator(rv, showIndication, R.id.event_entry_indicator_recurring, R.attr.eventEntryRecurring);
    }

    private void setIndicator(RemoteViews rv, boolean showIndication, int viewId, int imageAttrId) {
        if (showIndication) {
            rv.setViewVisibility(viewId, View.VISIBLE);
            setImageFromAttr(context, rv, viewId, imageAttrId);
            int themeId = getCurrentThemeId(context, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT);
            int alpha = 255;
            if (themeId == R.style.Theme_Calendar_Dark || themeId == R.style.Theme_Calendar_Light) {
                alpha = 128;
            }
            setAlpha(rv, viewId, alpha);
        } else {
            rv.setViewVisibility(viewId, View.GONE);
        }
    }

    private void setColor(CalendarEntry entry, RemoteViews rv) {
        setBackgroundColor(rv, R.id.event_entry_color, entry.getColor());
        if (entry.getEndDate().isBefore(DateUtil.now())) {
            setBackgroundColor(rv, R.id.event_entry, CalendarPreferences.getPastEventsBackgroundColor(context));
        } else {
            setBackgroundColor(rv, R.id.event_entry, 0);
        }
    }

    private Intent createOnItemClickIntent(CalendarEvent event) {
		return CalendarIntentUtil.createOpenCalendarEventIntent(event.getEventId(),
                event.getStartDate(), event.getEndDate());
	}

	private String createTimeSpanString(CalendarEntry entry) {
        if (entry.isAllDay() && !CalendarPreferences.getFillAllDayEvents(context)) {
            DateTime dateTime = entry.getEvent().getEndDate().minusDays(1);
            return ARROW_SPACE + DateUtil.createDateString(context, dateTime);
        } else {
            return createTimeStringForCalendarEntry(entry);
        }
    }

    private String createTimeStringForCalendarEntry(CalendarEntry entry) {
        String startStr;
        String endStr;
        String separator = SPACE_DASH_SPACE;
        if (entry.isPartOfMultiDayEvent()&& DateUtil.isMidnight(entry.getStartDate())
                && !entry.isStartOfMultiDayEvent()) {
            startStr = ARROW_SPACE;
            separator = EMPTY_STRING;
        } else {
            startStr = createTimeString(entry.getStartDate());
        }
        if (prefs.getBoolean(PREF_SHOW_END_TIME, PREF_SHOW_END_TIME_DEFAULT)) {
            if (entry.isPartOfMultiDayEvent() && DateUtil.isMidnight(entry.getEndDate())
                    && !entry.isEndOfMultiDayEvent()) {
                endStr = SPACE_ARROW;
                separator = EMPTY_STRING;
            } else {
                endStr = createTimeString(entry.getEndDate());
            }
        } else {
            separator = EMPTY_STRING;
            endStr = EMPTY_STRING;
        }
        return startStr + separator + endStr;
    }

    private String createTimeString(DateTime time) {
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
        return 1;
	}

	public List<CalendarEntry> getEventEntries() {
        List<CalendarEntry> entries = createEntryList(calendarContentProvider.getEvents());
        Collections.sort(entries);
        return entries;
	}

    private List<CalendarEntry> createEntryList(List<CalendarEvent> eventList) {
        boolean fillAllDayEvents = CalendarPreferences.getFillAllDayEvents(context);
        List<CalendarEntry> entryList = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            CalendarEntry dayOneEntry = setupDayOneEntry(entryList, event);
            if (fillAllDayEvents) {
                createFollowingEntries(entryList, dayOneEntry);
            }
        }
        return entryList;
    }

    private CalendarEntry setupDayOneEntry(List<CalendarEntry> entryList, CalendarEvent event) {
        CalendarEntry dayOneEntry = CalendarEntry.fromEvent(event);
        DateTime firstDate = dayOneEntry.getStartDate();
        if (!event.hasDefaultCalendarColor()
                && firstDate.isBefore(calendarContentProvider.getStartOfTimeRange())
                && event.getEndDate().isAfter(calendarContentProvider.getStartOfTimeRange())) {
            if (event.isAllDay()) {
                firstDate = calendarContentProvider.getStartOfTimeRange().withTimeAtStartOfDay();
            } else {
                firstDate = calendarContentProvider.getStartOfTimeRange();
            }
        }
        DateTime today = DateUtil.now().withTimeAtStartOfDay();
        if (event.isActive() && firstDate.isBefore(today)) {
            firstDate = today;
        }
        dayOneEntry.setStartDate(firstDate);
        DateTime nextDay = dayOneEntry.getStartDay().plusDays(1);
        boolean spanMoreDays = event.getEndDate().isAfter(nextDay);
        if (spanMoreDays) {
            dayOneEntry.setSpansMultipleDays();
            dayOneEntry.setEndDate(nextDay);
        }
        entryList.add(dayOneEntry);
        return dayOneEntry;
    }

    private void createFollowingEntries(List<CalendarEntry> entryList, CalendarEntry dayOneEntry) {
        DateTime endDate = dayOneEntry.getEvent().getEndDate();
        if (endDate.isAfter(calendarContentProvider.getEndOfTimeRange())) {
            endDate = calendarContentProvider.getEndOfTimeRange();
        }
        DateTime nextDay = dayOneEntry.getStartDay().plusDays(1).withTimeAtStartOfDay();
        while (nextDay.isBefore(endDate)) {
            CalendarEntry nextEntry = CalendarEntry.fromEvent(dayOneEntry.getEvent());
            nextEntry.setStartDate(nextDay);
            if (endDate.isAfter(nextDay)) {
                nextEntry.setEndDate(nextDay);
            } else {
                nextEntry.setEndDate(endDate);
            }
            entryList.add(nextEntry);
            nextDay = nextDay.plusDays(1);
        }
    }

	public Class<? extends CalendarEntry> getSupportedEventEntryType() {
		return CalendarEntry.class;
	}

}
