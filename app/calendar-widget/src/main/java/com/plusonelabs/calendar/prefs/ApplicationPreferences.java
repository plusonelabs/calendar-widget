package com.plusonelabs.calendar.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.plusonelabs.calendar.Alignment;
import com.plusonelabs.calendar.EndedSomeTimeAgo;
import com.plusonelabs.calendar.Theme;
import com.plusonelabs.calendar.widget.EventEntryLayout;

import java.util.HashSet;
import java.util.Set;

public class ApplicationPreferences {

    public static final String PREF_WIDGET_ID = "widgetId";

    static final String PREF_TEXT_SIZE_SCALE = "textSizeScale";
    static final String PREF_TEXT_SIZE_SCALE_DEFAULT = "1.0";
    static final String PREF_MULTILINE_TITLE = "multiline_title";
    static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
    static final String PREF_ACTIVE_CALENDARS = "activeCalendars";
    static final String PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents";
    static final String PREF_SHOW_DAY_HEADERS = "showDayHeaders";
    static final String PREF_SHOW_WIDGET_HEADER = "showHeader";
    static final String PREF_INDICATE_RECURRING = "indicateRecurring";
    static final String PREF_INDICATE_ALERTS = "indicateAlerts";
    static final String PREF_BACKGROUND_COLOR = "backgroundColor";
    static final int PREF_BACKGROUND_COLOR_DEFAULT = 0x80000000;
    static final String PREF_DATE_FORMAT = "dateFormat";
    static final String PREF_DATE_FORMAT_DEFAULT = "auto";
    static final String PREF_EVENT_RANGE = "eventRange";
    static final String PREF_EVENT_RANGE_DEFAULT = "30";
    static final String PREF_EVENTS_ENDED = "eventsEnded";
    static final String PREF_SHOW_END_TIME = "showEndTime";
    static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
    static final String PREF_SHOW_LOCATION = "showLocation";
    static final boolean PREF_SHOW_LOCATION_DEFAULT = true;
    static final String PREF_FILL_ALL_DAY = "fillAllDay";
    static final boolean PREF_FILL_ALL_DAY_DEFAULT = true;
    static final String PREF_ENTRY_THEME = "entryTheme";
    public static final String PREF_ENTRY_THEME_DEFAULT = Theme.BLACK.name();
    static final String PREF_HEADER_THEME = "headerTheme";
    static final String PREF_HEADER_THEME_DEFAULT = Theme.DARK.name();
    static final String PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment";
    static final String PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.RIGHT.name();
    static final String PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR = "showPastEventsWithDefaultColor";
    static final String PREF_PAST_EVENTS_BACKGROUND_COLOR = "pastEventsBackgroundColor";
    static final int PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT = 0x4affff2b;
    static final String PREF_HIDE_BASED_ON_KEYWORDS = "hideBasedOnKeywords";
    static final String KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging";
    static final String PREF_ABBREVIATE_DATES = "abbreviateDates";
    static final boolean PREF_ABBREVIATE_DATES_DEFAULT = false;
    static final String PREF_LOCK_TIME_ZONE = "lockTimeZone";
    static final String PREF_LOCKED_TIME_ZONE_ID = "lockedTimeZoneId";
    static final String PREF_EVENT_ENTRY_LAYOUT = "eventEntryLayout";
    static final String PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT =
            "showOnlyClosestInstanceOfRecurringEvent";
    static final String PREF_WIDGET_INSTANCE_NAME = "widgetInstanceName";

    private static volatile String lockedTimeZoneId = null;

    private ApplicationPreferences() {
        // prohibit instantiation
    }

    public static void startEditing(Context context, Integer widgetId) {
        InstanceSettings settings = InstanceSettings.fromId(context, widgetId);
        setWidgetId(context, settings.getWidgetId());
        setString(context, PREF_WIDGET_INSTANCE_NAME, settings.getWidgetInstanceName());
        setActiveCalendars(context, settings.getActiveCalendars());
        setEventRange(context, settings.getEventRange());
        setEventsEnded(context, settings.getEventsEnded());
        setFillAllDayEvents(context, settings.getFillAllDayEvents());
        setHideBasedOnKeywords(context, settings.getHideBasedOnKeywords());
        setInt(context, PREF_PAST_EVENTS_BACKGROUND_COLOR, settings.getPastEventsBackgroundColor());
        setShowDaysWithoutEvents(context, settings.getShowDaysWithoutEvents());
        setShowDayHeaders(context, settings.getShowDayHeaders());
        setShowPastEventsWithDefaultColor(context, settings.getShowPastEventsWithDefaultColor());
        setBoolean(context, PREF_SHOW_END_TIME, settings.getShowEndTime());
        setBoolean(context, PREF_SHOW_LOCATION, settings.getShowLocation());
        setString(context, PREF_DATE_FORMAT, settings.getDateFormat());
        setAbbreviateDates(context, settings.getAbbreviateDates());
        setLockedTimeZoneId(context, settings.getLockedTimeZoneId());
        setString(context, PREF_EVENT_ENTRY_LAYOUT, settings.getEventEntryLayout().value);
        setBoolean(context, PREF_MULTILINE_TITLE, settings.isTitleMultiline());
        setBoolean(context, PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, settings
                .getShowOnlyClosestInstanceOfRecurringEvent());
        setBoolean(context, PREF_INDICATE_ALERTS, settings.getIndicateAlerts());
        setBoolean(context, PREF_INDICATE_RECURRING, settings.getIndicateRecurring());
        setString(context, PREF_ENTRY_THEME, settings.getEntryTheme());
        setString(context, PREF_HEADER_THEME, settings.getHeaderTheme());
        setBoolean(context, PREF_SHOW_WIDGET_HEADER, settings.getShowWidgetHeader());
        setInt(context, PREF_BACKGROUND_COLOR, settings.getBackgroundColor());
        setString(context, PREF_TEXT_SIZE_SCALE, settings.getTextSizeScale());
        setString(context, PREF_DAY_HEADER_ALIGNMENT, settings.getDayHeaderAlignment());
    }

    public static void save(Context context, int wigdetId) {
        if (wigdetId != 0 && wigdetId == getWidgetId(context)) {
            InstanceSettings.save(context, wigdetId);
        }
    }

    public static InstanceSettings currentSettings(Context context) {
        return InstanceSettings.fromId(context, getWidgetId(context));
    }

    public static int getWidgetId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_WIDGET_ID, 0);
    }

    public static void setWidgetId(Context context, int value) {
        setInt(context, PREF_WIDGET_ID, value);
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
        setString(context, PREF_EVENT_RANGE, Integer.toString(value));
    }

    public static EndedSomeTimeAgo getEventsEnded(Context context) {
        return EndedSomeTimeAgo.fromValue(PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_EVENTS_ENDED, ""));
    }

    public static void setEventsEnded(Context context, EndedSomeTimeAgo value) {
        setString(context, PREF_EVENTS_ENDED, value.save());
    }

    public static boolean getFillAllDayEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_FILL_ALL_DAY, PREF_FILL_ALL_DAY_DEFAULT);
    }

    private static void setFillAllDayEvents(Context context, boolean value) {
        setBoolean(context, PREF_FILL_ALL_DAY, value);
    }

    public static String getHideBasedOnKeywords(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_HIDE_BASED_ON_KEYWORDS, "");
    }

    private static void setHideBasedOnKeywords(Context context, String value) {
        setString(context, PREF_HIDE_BASED_ON_KEYWORDS, value);
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
        setBoolean(context, PREF_SHOW_DAYS_WITHOUT_EVENTS, value);
    }

    public static boolean getShowDayHeaders(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_DAY_HEADERS, true);
    }

    private static void setShowDayHeaders(Context context, boolean value) {
        setBoolean(context, PREF_SHOW_DAY_HEADERS, value);
    }

    public static boolean getShowPastEventsWithDefaultColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, false);
    }

    public static void setShowPastEventsWithDefaultColor(Context context, boolean value) {
        setBoolean(context, PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, value);
    }

    public static boolean getShowEndTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_END_TIME, PREF_SHOW_END_TIME_DEFAULT);
    }

    public static boolean getShowLocation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_LOCATION, PREF_SHOW_LOCATION_DEFAULT);
    }

    public static String getDateFormat(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_DATE_FORMAT, PREF_DATE_FORMAT_DEFAULT);
    }

    public static boolean getAbbreviateDates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_ABBREVIATE_DATES, PREF_ABBREVIATE_DATES_DEFAULT);
    }

    public static void setAbbreviateDates(Context context, boolean value) {
        setBoolean(context, PREF_ABBREVIATE_DATES, value);
    }

    public static String getLockedTimeZoneId(Context context) {
        if (lockedTimeZoneId != null) {
            return lockedTimeZoneId;
        }
        lockedTimeZoneId = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LOCKED_TIME_ZONE_ID, "");
        return lockedTimeZoneId;
    }

    public static void setLockedTimeZoneId(Context context, String value) {
        lockedTimeZoneId = value;
        setString(context, PREF_LOCKED_TIME_ZONE_ID, value);
    }

    public static boolean isTimeZoneLocked(Context context) {
        return !TextUtils.isEmpty(getLockedTimeZoneId(context));
    }

    public static EventEntryLayout getEventEntryLayout(Context context) {
        return EventEntryLayout.fromValue(PreferenceManager.getDefaultSharedPreferences(context).getString(
                PREF_EVENT_ENTRY_LAYOUT, ""));
    }

    public static boolean isTitleMultiline(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_MULTILINE_TITLE, PREF_MULTILINE_TITLE_DEFAULT);
    }

    public static boolean getShowOnlyClosestInstanceOfRecurringEvent(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, false);
    }

    public static void setShowOnlyClosestInstanceOfRecurringEvent(Context context, boolean value) {
        setBoolean(context, PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, value);
    }

    private static void setString(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    private static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    public static String getWidgetInstanceName(Context context) {
        return getString(context, PREF_WIDGET_INSTANCE_NAME, "");
    }
}
