package com.plusonelabs.calendar.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.plusonelabs.calendar.Alignment;
import com.plusonelabs.calendar.EndedSometimeAgo;
import com.plusonelabs.calendar.Theme;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class CalendarPreferences {

	public static final String PREF_TEXT_SIZE_SCALE = "textSizeScale";
	public static final String PREF_TEXT_SIZE_SCALE_DEFAULT = "1.0";
	public static final String PREF_MULTILINE_TITLE = "multiline_title";
	public static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
	private static final String PREF_ACTIVE_CALENDARS = "activeCalendars";
	private static final String PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents";
	public static final String PREF_SHOW_HEADER = "showHeader";
	public static final String PREF_INDICATE_RECURRING = "indicateRecurring";
	public static final String PREF_INDICATE_ALERTS = "indicateAlerts";
	public static final String PREF_INDICATE_UNDECIDED = "indicateUndecided";
	public static final String PREF_BACKGROUND_COLOR = "backgroundColor";
	public static final int PREF_BACKGROUND_COLOR_DEFAULT = 0x80000000;
	public static final String PREF_DATE_FORMAT = "dateFormat";
	public static final String PREF_DATE_FORMAT_DEFAULT = "auto";
	public static final String PREF_EVENT_RANGE = "eventRange";
	private static final String PREF_EVENT_RANGE_DEFAULT = "30";
	public static final String PREF_EVENTS_ENDED = "eventsEnded";
	public static final String PREF_SHOW_END_TIME = "showEndTime";
	public static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
	public static final String PREF_SHOW_LOCATION = "showLocation";
	public static final boolean PREF_SHOW_LOCATION_DEFAULT = true;
	private static final String PREF_FILL_ALL_DAY = "fillAllDay";
	private static final boolean PREF_FILL_ALL_DAY_DEFAULT = true;
	public static final String PREF_ENTRY_THEME = "entryTheme";
	public static final String PREF_ENTRY_THEME_DEFAULT = Theme.BLACK.name();
	public static final String PREF_HEADER_THEME = "headerTheme";
	public static final String PREF_HEADER_THEME_DEFAULT = Theme.DARK.name();
	public static final String PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment";
	public static final String PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.RIGHT.name();
    private static final String PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR = "showPastEventsWithDefaultColor";
    public static final String PREF_PAST_EVENTS_BACKGROUND_COLOR = "pastEventsBackgroundColor";
    public static final int PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT = 0x4affff2b;
	public static final String PREF_HIDE_BASED_ON_KEYWORDS = "hideBasedOnKeywords";

	private CalendarPreferences() {
		// prohibit instantiation
	}

    public static JSONObject toJson(Context context) throws JSONException {
        JSONObject jso = new JSONObject();
        jso.put(PREF_EVENT_RANGE, getEventRange(context));
        jso.put(PREF_EVENTS_ENDED, getEventsEnded(context));
        jso.put(PREF_FILL_ALL_DAY, getFillAllDayEvents(context));
        jso.put(PREF_HIDE_BASED_ON_KEYWORDS, getHideBasedOnKeywords(context));
        jso.put(PREF_SHOW_DAYS_WITHOUT_EVENTS, getShowDaysWithoutEvents(context));
        jso.put(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, getShowPastEventsWithDefaultColor(context));
        return jso;
    }

    public static void fromJson(Context context, JSONObject jso) throws JSONException {
        setEventRange(context, jso.getInt(PREF_EVENT_RANGE));
        setEventsEnded(context, EndedSometimeAgo.fromValue(jso.getString(PREF_EVENTS_ENDED)));
        setFillAllDayEvents(context, jso.getBoolean(PREF_FILL_ALL_DAY));
        setHideBasedOnKeywords(context, jso.getString(PREF_HIDE_BASED_ON_KEYWORDS));
        setShowDaysWithoutEvents(context, jso.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS));
        setShowPastEventsWithDefaultColor(context, jso.getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR));
    }

    public static Set<String> getActiveCalendars(Context context) {
        Set<String> activeCalendars = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(PREF_ACTIVE_CALENDARS, null);
        if (activeCalendars == null) {
            activeCalendars = new HashSet<>();
        }
        return activeCalendars;
    }

    public static void setActiveCalendars(Context context, Set<String> calendars) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(PREF_ACTIVE_CALENDARS, calendars);
        editor.apply();
    }

    public static int getEventRange(Context context) {
        return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_EVENT_RANGE, PREF_EVENT_RANGE_DEFAULT));
    }

    public static void setEventRange(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_EVENT_RANGE, Integer.toString(value));
        editor.apply();
    }

    public static EndedSometimeAgo getEventsEnded(Context context) {
        return EndedSometimeAgo.fromValue(PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_EVENTS_ENDED, ""));
    }

    public static void setEventsEnded(Context context, EndedSometimeAgo value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_EVENTS_ENDED, value.save());
        editor.apply();
    }

    public static boolean getFillAllDayEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT);
    }

    private static void setFillAllDayEvents(Context context, boolean value) {
        setBooleanPreference(context, PREF_FILL_ALL_DAY, value);
    }

    public static String getHideBasedOnKeywords(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_HIDE_BASED_ON_KEYWORDS,
                "");
    }

    private static void setHideBasedOnKeywords(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_HIDE_BASED_ON_KEYWORDS, value);
        editor.apply();
    }

    public static int getPastEventsBackgroundColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(
                PREF_PAST_EVENTS_BACKGROUND_COLOR,
                PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT);
    }

    public static boolean getShowDaysWithoutEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS, false);
    }

    private static void setShowDaysWithoutEvents(Context context, boolean value) {
        setBooleanPreference(context, PREF_SHOW_DAYS_WITHOUT_EVENTS, value);
    }

    public static boolean getShowPastEventsWithDefaultColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, false);
    }

    public static void setShowPastEventsWithDefaultColor(Context context, boolean value) {
        setBooleanPreference(context, PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, value);
    }

    private static void setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

}
