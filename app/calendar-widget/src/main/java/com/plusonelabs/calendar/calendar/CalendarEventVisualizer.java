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
import com.plusonelabs.calendar.model.Event;

import org.joda.time.DateTime;

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
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_FILL_ALL_DAY;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_FILL_ALL_DAY_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_INDICATE_ALERTS;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_INDICATE_RECURRING;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_MULTILINE_TITLE;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_MULTILINE_TITLE_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_END_TIME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_END_TIME_DEFAULT;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_LOCATION;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_SHOW_LOCATION_DEFAULT;

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
        setTextColorFromAttr(context, rv, R.id.event_entry_title, R.attr.eventEntryTitle);
        setSingleLine(rv, R.id.event_entry_title,
                !prefs.getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT));
    }

	private void setEventDetails(CalendarEvent event, RemoteViews rv) {
        boolean fillAllDayEvents = prefs.getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT);
        if (event.spansOneFullDay() && !(event.isStartOfMultiDayEvent()
                || event.isEndOfMultiDayEvent())
                || event.isAllDay() && fillAllDayEvents) {
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
            setTextColorFromAttr(context, rv, R.id.event_entry_details, R.attr.eventEntryDetails);
        }
    }

    private void setAlarmActive(CalendarEvent event, RemoteViews rv) {
        boolean showIndication = event.isAlarmActive() && prefs.getBoolean(PREF_INDICATE_ALERTS, true);
        setIndicator(rv, showIndication, R.id.event_entry_indicator_alarm, R.attr.eventEntryAlarm);
	}

	private void setRecurring(CalendarEvent event, RemoteViews rv) {
        boolean showIndication = event.isRecurring() && prefs.getBoolean(PREF_INDICATE_RECURRING, false);
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

    private void setColor(CalendarEvent event, RemoteViews rv) {
        setBackgroundColor(rv, R.id.event_entry_color, event.getColor());
    }

    private Intent createOnItemClickIntent(CalendarEvent event) {
		CalendarEvent originalEvent = event.getOriginalEvent();
		if (originalEvent != null) {
			event = originalEvent;
		}
		return CalendarIntentUtil.createOpenCalendarEventIntent(event.getEventId(),
				event.getStartDate(), event.getEndDate());
	}

	private String createTimeSpanString(CalendarEvent event) {
        if (event.isAllDay() && !prefs.getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT)) {
            DateTime dateTime = event.getOriginalEvent().getEndDate().minusDays(1);
            return ARROW_SPACE + EMPTY_STRING + DateUtil.createDateString(context, dateTime);
        } else {
            return createTimeStringForEventEntry(event);
        }
    }

    private String createTimeStringForEventEntry(CalendarEvent event) {
        String startStr;
        String endStr;
        String separator = SPACE_DASH_SPACE;
        if (event.isPartOfMultiDayEvent()&& DateUtil.isMidnight(event.getStartDate())
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

        if (startStr.equals(endStr)) {
            return startStr;
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

	public List<CalendarEvent> getEventEntries() {
		return calendarContentProvider.getEvents();
	}

	public Class<? extends CalendarEvent> getSupportedEventEntryType() {
		return CalendarEvent.class;
	}

}
