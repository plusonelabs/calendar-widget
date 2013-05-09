package com.plusonelabs.calendar.prefs;

public class CalendarPreferences {

	public static final String PREF_TEXT_SIZE_SCALE = "textSizeScale";
	public static final String PREF_TEXT_SIZE_SCALE_DEFAULT = "1.0";
	public static final String PREF_MULTILINE_TITLE = "multiline_title";
	public static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
	public static final String PREF_ACTIVE_CALENDARS = "activeCalendars";
	public static final String PREF_SHOW_HEADER = "showHeader";
	public static final String PREF_INDICATE_RECURRING = "indicateRecurring";
	public static final String PREF_INDICATE_ALERTS = "indicateAlerts";
	public static final String PREF_BACKGROUND_TRANSPARENCY = "backgroundTransparency";
	public static final int PREF_BACKGROUND_TRANSPARENCY_DEFAULT = 50;
	public static final String PREF_DATE_FORMAT = "dateFormat";
	public static final String PREF_DATE_FORMAT_DEFAULT = "auto";
	public static final String PREF_EVENT_RANGE = "eventRange";
	public static final String PREF_EVENT_RANGE_DEFAULT = "30";
	public static final String PREF_SHOW_END_TIME = "showEndTime";
	public static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
	public static final String PREF_SHOW_LOCATION = "showLocation";
	public static final boolean PREF_SHOW_LOCATION_DEFAULT = true;

	private CalendarPreferences() {
		// prohibit instantiation
	}
}
