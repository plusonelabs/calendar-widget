package com.plusonelabs.calendar.prefs;

import com.plusonelabs.calendar.Alignment;
import com.plusonelabs.calendar.Theme;

public class CalendarPreferences {

	public static final String PREF_TEXT_SIZE_SCALE = "textSizeScale";
	public static final String PREF_TEXT_SIZE_SCALE_DEFAULT = "1.0";
	public static final String PREF_MULTILINE_TITLE = "multiline_title";
	public static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
	public static final String PREF_ACTIVE_CALENDARS = "activeCalendars";
	public static final String PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents";
	public static final String PREF_SHOW_HEADER = "showHeader";
	public static final String PREF_INDICATE_RECURRING = "indicateRecurring";
	public static final String PREF_INDICATE_ALERTS = "indicateAlerts";
	public static final String PREF_BACKGROUND_COLOR = "backgroundColor";
	public static final int PREF_BACKGROUND_COLOR_DEFAULT = 0x80000000;
	public static final String PREF_DATE_FORMAT = "dateFormat";
	public static final String PREF_DATE_FORMAT_DEFAULT = "auto";
	public static final String PREF_EVENT_RANGE = "eventRange";
	public static final String PREF_EVENT_RANGE_DEFAULT = "30";
	public static final String PREF_SHOW_END_TIME = "showEndTime";
	public static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
	public static final String PREF_SHOW_LOCATION = "showLocation";
	public static final boolean PREF_SHOW_LOCATION_DEFAULT = true;
	public static final String PREF_FILL_ALL_DAY = "fillAllDay";
	public static final boolean PREF_FILL_ALL_DAY_DEFAULT = true;
	public static final String PREF_ENTRY_THEME = "entryTheme";
	public static final String PREF_ENTRY_THEME_DEFAULT = Theme.BLACK.name();
	public static final String PREF_HEADER_THEME = "headerTheme";
	public static final String PREF_HEADER_THEME_DEFAULT = Theme.DARK.name();
	public static final String PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment";
	public static final String PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.RIGHT.name();

	private CalendarPreferences() {
		// prohibit instantiation
	}
}
