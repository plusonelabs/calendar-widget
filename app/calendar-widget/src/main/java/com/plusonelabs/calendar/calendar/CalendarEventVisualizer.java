package com.plusonelabs.calendar.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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

import static com.plusonelabs.calendar.RemoteViewsUtil.setAlpha;
import static com.plusonelabs.calendar.RemoteViewsUtil.setBackgroundColor;
import static com.plusonelabs.calendar.RemoteViewsUtil.setImageFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setSingleLine;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextColorFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextSize;
import static com.plusonelabs.calendar.Theme.getCurrentThemeId;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_INDICATE_ALERTS;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_INDICATE_RECURRING;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_MULTILINE_TITLE;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_MULTILINE_TITLE_DEFAULT;

public class CalendarEventVisualizer implements IEventVisualizer<CalendarEntry> {
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
		rv.setTextViewText(R.id.event_entry_title, event.getTitle(context));
		setTextSize(context, rv, R.id.event_entry_title, R.dimen.event_entry_title);
        setTextColorFromAttr(context, rv, R.id.event_entry_title, R.attr.eventEntryTitle);
        setSingleLine(rv, R.id.event_entry_title,
                !prefs.getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT));
    }

	private void setEventDetails(CalendarEntry entry, RemoteViews rv) {
        String eventDetails = entry.getEventDetails(context);
        if (TextUtils.isEmpty(eventDetails)) {
            rv.setViewVisibility(R.id.event_entry_details, View.GONE);
        } else {
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
        DateTime firstDate = event.getStartDate();
        DateTime dayOfStartOfTimeRange = calendarContentProvider.getStartOfTimeRange()
                .withTimeAtStartOfDay();
        if (!event.hasDefaultCalendarColor()
                && firstDate.isBefore(calendarContentProvider.getStartOfTimeRange())
                && event.getEndDate().isAfter(calendarContentProvider.getStartOfTimeRange())) {
            if (event.isAllDay() || firstDate.isBefore(dayOfStartOfTimeRange)) {
                firstDate = dayOfStartOfTimeRange;
            }
        }
        DateTime today = DateUtil.now().withTimeAtStartOfDay();
        if (event.isActive() && firstDate.isBefore(today)) {
            firstDate = today;
        }
        dayOneEntry.setStartDate(firstDate);
        DateTime nextDay = dayOneEntry.getStartDay().plusDays(1);
        if (event.getEndDate().isAfter(nextDay)) {
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
        DateTime thisDay = dayOneEntry.getStartDay().plusDays(1).withTimeAtStartOfDay();
        while (thisDay.isBefore(endDate)) {
            DateTime nextDay = thisDay.plusDays(1);
            CalendarEntry nextEntry = CalendarEntry.fromEvent(dayOneEntry.getEvent());
            nextEntry.setStartDate(thisDay);
            if (endDate.isAfter(nextDay)) {
                nextEntry.setEndDate(nextDay);
            } else {
                nextEntry.setEndDate(endDate);
            }
            entryList.add(nextEntry);
            thisDay = nextDay;
        }
    }

	public Class<? extends CalendarEntry> getSupportedEventEntryType() {
		return CalendarEntry.class;
	}

}
