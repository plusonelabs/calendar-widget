package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;

import org.andstatus.todoagenda.EndedSomeTimeAgo;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.EventEntryLayout;
import org.andstatus.todoagenda.widget.WidgetHeaderLayout;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;

import static org.andstatus.todoagenda.Theme.themeNameToResId;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_ABBREVIATE_DATES;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_ABBREVIATE_DATES_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_ACTIVE_SOURCES;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DATE_FORMAT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DATE_FORMAT_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DAY_HEADER_ALIGNMENT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DAY_HEADER_ALIGNMENT_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DAY_HEADER_THEME;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DAY_HEADER_THEME_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_ENTRY_THEME;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_ENTRY_THEME_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_EVENTS_BACKGROUND_COLOR;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_EVENTS_BACKGROUND_COLOR_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_EVENTS_ENDED;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_EVENT_ENTRY_LAYOUT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_EVENT_RANGE;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_EVENT_RANGE_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_FILL_ALL_DAY;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_FILL_ALL_DAY_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_HIDE_BASED_ON_KEYWORDS;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_INDICATE_ALERTS;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_INDICATE_RECURRING;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_LOCKED_TIME_ZONE_ID;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_MULTILINE_TITLE;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_MULTILINE_TITLE_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_DAYS_WITHOUT_EVENTS;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_DAY_HEADERS;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_END_TIME;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_END_TIME_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_EVENT_ICON;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_LOCATION;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_LOCATION_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_SHOW_WIDGET_HEADER;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_TEXT_SIZE_SCALE;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_TEXT_SIZE_SCALE_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_TODAYS_EVENTS_BACKGROUND_COLOR;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_TODAYS_EVENTS_BACKGROUND_COLOR_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_WIDGET_HEADER_BACKGROUND_COLOR;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_WIDGET_HEADER_BACKGROUND_COLOR_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_WIDGET_HEADER_LAYOUT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_WIDGET_HEADER_THEME;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_WIDGET_HEADER_THEME_DEFAULT;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_WIDGET_ID;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_WIDGET_INSTANCE_NAME;
import static org.andstatus.todoagenda.prefs.SettingsStorage.saveJson;

/**
 * Loaded settings of one Widget
 * @author yvolk@yurivolkov.com
 */
public class InstanceSettings {
    private final Context context;
    private volatile ContextThemeWrapper widgetHeaderThemeContext = null;
    private volatile ContextThemeWrapper dayHeaderThemeContext = null;
    private volatile ContextThemeWrapper entryThemeContext = null;

    final int widgetId;
    private final String widgetInstanceName;
    private List<EventSource> activeEventSources = Collections.emptyList();
    private int eventRange = Integer.valueOf(PREF_EVENT_RANGE_DEFAULT);
    private EndedSomeTimeAgo eventsEnded = EndedSomeTimeAgo.NONE;
    private boolean fillAllDayEvents = PREF_FILL_ALL_DAY_DEFAULT;
    private String hideBasedOnKeywords = "";
    private int widgetHeaderBackgroundColor = PREF_WIDGET_HEADER_BACKGROUND_COLOR_DEFAULT;
    private int pastEventsBackgroundColor = PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT;
    private int todaysEventsBackgroundColor = PREF_TODAYS_EVENTS_BACKGROUND_COLOR_DEFAULT;
    private int eventsBackgroundColor = PREF_EVENTS_BACKGROUND_COLOR_DEFAULT;
    private boolean showDaysWithoutEvents = false;
    private boolean showDayHeaders = true;
    private boolean showPastEventsUnderOneHeader = false;
    private boolean showPastEventsWithDefaultColor = false;
    private boolean showNumberOfDaysToEvent = true;
    private boolean showEventIcon = true;
    private boolean showEndTime = PREF_SHOW_END_TIME_DEFAULT;
    private boolean showLocation = PREF_SHOW_LOCATION_DEFAULT;
    private String dateFormat = PREF_DATE_FORMAT_DEFAULT;
    private boolean abbreviateDates = PREF_ABBREVIATE_DATES_DEFAULT;
    private String lockedTimeZoneId = "";
    private EventEntryLayout eventEntryLayout = EventEntryLayout.DEFAULT;
    private boolean titleMultiline = PREF_MULTILINE_TITLE_DEFAULT;
    private boolean showOnlyClosestInstanceOfRecurringEvent = false;
    private boolean indicateAlerts = true;
    private boolean indicateRecurring = false;
    private WidgetHeaderLayout widgetHeaderLayout = WidgetHeaderLayout.defaultValue;
    private String widgetHeaderTheme = PREF_WIDGET_HEADER_THEME_DEFAULT;
    private String dayHeaderTheme = PREF_DAY_HEADER_THEME_DEFAULT;
    private String entryTheme = PREF_ENTRY_THEME_DEFAULT;
    private String textSizeScale = PREF_TEXT_SIZE_SCALE_DEFAULT;
    private String dayHeaderAlignment = PREF_DAY_HEADER_ALIGNMENT_DEFAULT;

    public static InstanceSettings fromJson(Context context, JSONObject json) throws JSONException {
        InstanceSettings settings = new InstanceSettings(context, json.optInt(PREF_WIDGET_ID),
                json.optString(PREF_WIDGET_INSTANCE_NAME));
        if (settings.widgetId == 0) {
            return settings;
        }
        if (json.has(PREF_ACTIVE_SOURCES)) {
            JSONArray jsonArray = json.getJSONArray(PREF_ACTIVE_SOURCES);
            settings.setActiveEventSources(EventSource.fromJsonArray(jsonArray));
        }
        if (json.has(PREF_EVENT_RANGE)) {
            settings.eventRange = json.getInt(PREF_EVENT_RANGE);
        }
        if (json.has(PREF_EVENTS_ENDED)) {
            settings.eventsEnded = EndedSomeTimeAgo.fromValue(json.getString(PREF_EVENTS_ENDED));
        }
        if (json.has(PREF_FILL_ALL_DAY)) {
            settings.fillAllDayEvents = json.getBoolean(PREF_FILL_ALL_DAY);
        }
        if (json.has(PREF_HIDE_BASED_ON_KEYWORDS)) {
            settings.hideBasedOnKeywords = json.getString(PREF_HIDE_BASED_ON_KEYWORDS);
        }
        if (json.has(PREF_WIDGET_HEADER_BACKGROUND_COLOR)) {
            settings.widgetHeaderBackgroundColor = json.getInt(PREF_WIDGET_HEADER_BACKGROUND_COLOR);
        }
        if (json.has(PREF_PAST_EVENTS_BACKGROUND_COLOR)) {
            settings.pastEventsBackgroundColor = json.getInt(PREF_PAST_EVENTS_BACKGROUND_COLOR);
        }
        if (json.has(PREF_TODAYS_EVENTS_BACKGROUND_COLOR)) {
            settings.todaysEventsBackgroundColor = json.getInt(PREF_TODAYS_EVENTS_BACKGROUND_COLOR);
        }
        if (json.has(PREF_EVENTS_BACKGROUND_COLOR)) {
            settings.eventsBackgroundColor = json.getInt(PREF_EVENTS_BACKGROUND_COLOR);
        }
        if (json.has(PREF_SHOW_DAYS_WITHOUT_EVENTS)) {
            settings.showDaysWithoutEvents = json.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS);
        }
        if (json.has(PREF_SHOW_DAY_HEADERS)) {
            settings.showDayHeaders = json.getBoolean(PREF_SHOW_DAY_HEADERS);
        }
        if (json.has(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER)) {
            settings.showPastEventsUnderOneHeader = json.getBoolean(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER);
        }
        if (json.has(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR)) {
            settings.showPastEventsWithDefaultColor = json.getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR);
        }
        if (json.has(PREF_SHOW_EVENT_ICON)) {
            settings.showEventIcon = json.getBoolean(PREF_SHOW_EVENT_ICON);
        }
        if (json.has(PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT)) {
            settings.showNumberOfDaysToEvent = json.getBoolean(PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT);
        }
        if (json.has(PREF_SHOW_END_TIME)) {
            settings.showEndTime = json.getBoolean(PREF_SHOW_END_TIME);
        }
        if (json.has(PREF_SHOW_LOCATION)) {
            settings.showLocation = json.getBoolean(PREF_SHOW_LOCATION);
        }
        if (json.has(PREF_DATE_FORMAT)) {
            settings.dateFormat = json.getString(PREF_DATE_FORMAT);
        }
        if (json.has(PREF_ABBREVIATE_DATES)) {
            settings.abbreviateDates = json.getBoolean(PREF_ABBREVIATE_DATES);
        }
        if (json.has(PREF_LOCKED_TIME_ZONE_ID)) {
            settings.setLockedTimeZoneId(json.getString(PREF_LOCKED_TIME_ZONE_ID));
        }
        if (json.has(PREF_EVENT_ENTRY_LAYOUT)) {
            settings.eventEntryLayout = EventEntryLayout.fromValue(json.getString(PREF_EVENT_ENTRY_LAYOUT));
        }
        if (json.has(PREF_MULTILINE_TITLE)) {
            settings.titleMultiline = json.getBoolean(PREF_MULTILINE_TITLE);
        }
        if (json.has(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT)) {
            settings.showOnlyClosestInstanceOfRecurringEvent = json.getBoolean(
                    PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT);
        }
        if (json.has(PREF_INDICATE_ALERTS)) {
            settings.indicateAlerts = json.getBoolean(PREF_INDICATE_ALERTS);
        }
        if (json.has(PREF_INDICATE_RECURRING)) {
            settings.indicateRecurring = json.getBoolean(PREF_INDICATE_RECURRING);
        }
        if (json.has(PREF_WIDGET_HEADER_THEME)) {
            settings.widgetHeaderTheme = json.getString(PREF_WIDGET_HEADER_THEME);
        }
        if (json.has(PREF_WIDGET_HEADER_LAYOUT)) {
            settings.widgetHeaderLayout = WidgetHeaderLayout.fromValue(json.getString(PREF_WIDGET_HEADER_LAYOUT));
        } else if (json.has(PREF_SHOW_WIDGET_HEADER)) {
            settings.widgetHeaderLayout = json.getBoolean(PREF_SHOW_WIDGET_HEADER)
                ? WidgetHeaderLayout.defaultValue : WidgetHeaderLayout.HIDDEN;
        }
        if (json.has(PREF_DAY_HEADER_THEME)) {
            settings.dayHeaderTheme = json.getString(PREF_DAY_HEADER_THEME);
        }
        if (json.has(PREF_ENTRY_THEME)) {
            settings.entryTheme = json.getString(PREF_ENTRY_THEME);
        }
        if (json.has(PREF_TEXT_SIZE_SCALE)) {
            settings.textSizeScale = json.getString(PREF_TEXT_SIZE_SCALE);
        }
        if (json.has(PREF_DAY_HEADER_ALIGNMENT)) {
            settings.dayHeaderAlignment = json.getString(PREF_DAY_HEADER_ALIGNMENT);
        }
        return settings;
    }

    static InstanceSettings fromApplicationPreferences(Context context, int widgetId) {
        InstanceSettings settings = new InstanceSettings(context, widgetId,
                ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME,
                        ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME, "")));
        settings.setActiveEventSources(ApplicationPreferences.getActiveEventSources(context));
        settings.eventRange = ApplicationPreferences.getEventRange(context);
        settings.eventsEnded = ApplicationPreferences.getEventsEnded(context);
        settings.fillAllDayEvents = ApplicationPreferences.getFillAllDayEvents(context);
        settings.hideBasedOnKeywords = ApplicationPreferences.getHideBasedOnKeywords(context);
        settings.widgetHeaderBackgroundColor = ApplicationPreferences.getWidgetHeaderBackgroundColor(context);
        settings.pastEventsBackgroundColor = ApplicationPreferences.getPastEventsBackgroundColor(context);
        settings.todaysEventsBackgroundColor = ApplicationPreferences.getTodaysEventsBackgroundColor(context);
        settings.eventsBackgroundColor = ApplicationPreferences.getEventsBackgroundColor(context);
        settings.showDaysWithoutEvents = ApplicationPreferences.getShowDaysWithoutEvents(context);
        settings.showDayHeaders = ApplicationPreferences.getShowDayHeaders(context);
        settings.showPastEventsUnderOneHeader = ApplicationPreferences.getShowPastEventsUnderOneHeader(context);
        settings.showPastEventsWithDefaultColor = ApplicationPreferences.getShowPastEventsWithDefaultColor(context);
        settings.showEventIcon = ApplicationPreferences.getShowEventIcon(context);
        settings.showNumberOfDaysToEvent = ApplicationPreferences.getShowNumberOfDaysToEvent(context);
        settings.showEndTime = ApplicationPreferences.getShowEndTime(context);
        settings.showLocation = ApplicationPreferences.getShowLocation(context);
        settings.dateFormat = ApplicationPreferences.getDateFormat(context);
        settings.abbreviateDates = ApplicationPreferences.getAbbreviateDates(context);
        settings.setLockedTimeZoneId(ApplicationPreferences.getLockedTimeZoneId(context));
        settings.eventEntryLayout = ApplicationPreferences.getEventEntryLayout(context);
        settings.titleMultiline = ApplicationPreferences.isTitleMultiline(context);
        settings.showOnlyClosestInstanceOfRecurringEvent = ApplicationPreferences
                .getShowOnlyClosestInstanceOfRecurringEvent(context);
        settings.indicateAlerts = ApplicationPreferences.getBoolean(context, PREF_INDICATE_ALERTS, true);
        settings.indicateRecurring = ApplicationPreferences.getBoolean(context, PREF_INDICATE_RECURRING, false);
        settings.widgetHeaderTheme = ApplicationPreferences.getString(context, PREF_WIDGET_HEADER_THEME, PREF_WIDGET_HEADER_THEME_DEFAULT);
        settings.widgetHeaderLayout = ApplicationPreferences.getWidgetHeaderLayout(context);
        settings.dayHeaderTheme = ApplicationPreferences.getString(context, PREF_DAY_HEADER_THEME, PREF_DAY_HEADER_THEME_DEFAULT);
        settings.entryTheme = ApplicationPreferences.getString(context, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT);
        settings.textSizeScale = ApplicationPreferences.getString(context, PREF_TEXT_SIZE_SCALE,
                PREF_TEXT_SIZE_SCALE_DEFAULT);
        settings.dayHeaderAlignment = ApplicationPreferences.getString(context, PREF_DAY_HEADER_ALIGNMENT,
                PREF_DAY_HEADER_ALIGNMENT_DEFAULT);
        return settings;
    }

    @NonNull
    private static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    InstanceSettings(Context context, int widgetId, String proposedInstanceName) {
        this.context = context;
        this.widgetId = widgetId;
        this.widgetInstanceName = AllSettings.uniqueInstanceName(context, widgetId, proposedInstanceName);
    }

    void save() {
        if (widgetId == 0) {
            logMe(InstanceSettings.class, "Skipped save", widgetId);
            return;
        }
        logMe(InstanceSettings.class, "save", widgetId);
        try {
            saveJson(context, getStorageKey(widgetId), toJson());
        } catch (IOException e) {
            Log.e("save", toString(), e);
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(PREF_WIDGET_ID, widgetId);
            json.put(PREF_WIDGET_INSTANCE_NAME, widgetInstanceName);
            json.put(PREF_ACTIVE_SOURCES, EventSource.toJsonArray(getActiveEventSources()));
            json.put(PREF_EVENT_RANGE, eventRange);
            json.put(PREF_EVENTS_ENDED, eventsEnded.save());
            json.put(PREF_FILL_ALL_DAY, fillAllDayEvents);
            json.put(PREF_HIDE_BASED_ON_KEYWORDS, hideBasedOnKeywords);
            json.put(PREF_WIDGET_HEADER_BACKGROUND_COLOR, widgetHeaderBackgroundColor);
            json.put(PREF_PAST_EVENTS_BACKGROUND_COLOR, pastEventsBackgroundColor);
            json.put(PREF_TODAYS_EVENTS_BACKGROUND_COLOR, todaysEventsBackgroundColor);
            json.put(PREF_EVENTS_BACKGROUND_COLOR, eventsBackgroundColor);
            json.put(PREF_SHOW_DAYS_WITHOUT_EVENTS, showDaysWithoutEvents);
            json.put(PREF_SHOW_DAY_HEADERS, showDayHeaders);
            json.put(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER, showPastEventsUnderOneHeader);
            json.put(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, showPastEventsWithDefaultColor);
            json.put(PREF_SHOW_EVENT_ICON, showEventIcon);
            json.put(PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT, showNumberOfDaysToEvent);
            json.put(PREF_SHOW_END_TIME, showEndTime);
            json.put(PREF_SHOW_LOCATION, showLocation);
            json.put(PREF_DATE_FORMAT, dateFormat);
            json.put(PREF_ABBREVIATE_DATES, abbreviateDates);
            json.put(PREF_LOCKED_TIME_ZONE_ID, lockedTimeZoneId);
            json.put(PREF_EVENT_ENTRY_LAYOUT, eventEntryLayout.value);
            json.put(PREF_MULTILINE_TITLE, titleMultiline);
            json.put(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, showOnlyClosestInstanceOfRecurringEvent);
            json.put(PREF_INDICATE_ALERTS, indicateAlerts);
            json.put(PREF_INDICATE_RECURRING, indicateRecurring);
            json.put(PREF_WIDGET_HEADER_LAYOUT, widgetHeaderLayout.value);
            json.put(PREF_WIDGET_HEADER_THEME, widgetHeaderTheme);
            json.put(PREF_DAY_HEADER_THEME, dayHeaderTheme);
            json.put(PREF_ENTRY_THEME, entryTheme);
            json.put(PREF_TEXT_SIZE_SCALE, textSizeScale);
            json.put(PREF_DAY_HEADER_ALIGNMENT, dayHeaderAlignment);
        } catch (JSONException e) {
            throw new RuntimeException("Saving settings to JSON", e);
        }
        return json;
    }

    public Context getContext() {
        return context;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public String getWidgetInstanceName() {
        return widgetInstanceName;
    }

    public void setActiveEventSources(List<EventSource> activeEventSources) {
        this.activeEventSources = activeEventSources;
    }

    public List<EventSource> getActiveEventSources(EventProviderType type) {
        List<EventSource> sources = new ArrayList<>();
        for(EventSource source: getActiveEventSources()) {
            if (source.providerType == type) sources.add(source);
        }
        return sources;
    }

    public List<EventSource> getActiveEventSources() {
        return activeEventSources.isEmpty()
                ? EventProviderType.getAvailableSources()
                : activeEventSources;
    }

    public int getEventRange() {
        return eventRange;
    }

    public EndedSomeTimeAgo getEventsEnded() {
        return eventsEnded;
    }

    public boolean getFillAllDayEvents() {
        return fillAllDayEvents;
    }

    public String getHideBasedOnKeywords() {
        return hideBasedOnKeywords;
    }


    public int getWidgetHeaderBackgroundColor() {
        return widgetHeaderBackgroundColor;
    }

    public int getPastEventsBackgroundColor() {
        return pastEventsBackgroundColor;
    }

    public int getTodaysEventsBackgroundColor() {
        return todaysEventsBackgroundColor;
    }

    public int getEventsBackgroundColor() {
        return eventsBackgroundColor;
    }

    public boolean getShowDaysWithoutEvents() {
        return showDaysWithoutEvents;
    }

    public boolean getShowDayHeaders() {
        return showDayHeaders;
    }

    public boolean getShowPastEventsUnderOneHeader() {
        return showPastEventsUnderOneHeader;
    }

    public boolean getShowPastEventsWithDefaultColor() {
        return showPastEventsWithDefaultColor;
    }

    public boolean getShowNumberOfDaysToEvent() {
        return showNumberOfDaysToEvent;
    }

    public boolean getShowEventIcon() {
        return showEventIcon;
    }

    public boolean getShowEndTime() {
        return showEndTime;
    }

    public boolean getShowLocation() {
        return showLocation;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public boolean getAbbreviateDates() {
        return abbreviateDates;
    }

    private void setLockedTimeZoneId(String lockedTimeZoneId) {
        this.lockedTimeZoneId = DateUtil.validatedTimeZoneId(lockedTimeZoneId);
    }

    public String getLockedTimeZoneId() {
        return lockedTimeZoneId;
    }

    public boolean isTimeZoneLocked() {
        return !TextUtils.isEmpty(lockedTimeZoneId);
    }

    public DateTimeZone getTimeZone() {
        return DateTimeZone.forID(DateUtil.validatedTimeZoneId(
                isTimeZoneLocked() ? lockedTimeZoneId : TimeZone.getDefault().getID()));
    }

    public EventEntryLayout getEventEntryLayout() {
        return eventEntryLayout;
    }

    public boolean isTitleMultiline() {
        return titleMultiline;
    }

    public boolean getShowOnlyClosestInstanceOfRecurringEvent() {
        return showOnlyClosestInstanceOfRecurringEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceSettings settings = (InstanceSettings) o;
        return toJson().toString().equals(settings.toJson().toString());
    }

    @Override
    public int hashCode() {
        return toJson().toString().hashCode();
    }

    public boolean getIndicateAlerts() {
        return indicateAlerts;
    }

    public boolean getIndicateRecurring() {
        return indicateRecurring;
    }

    public String getWidgetHeaderTheme() {
        return widgetHeaderTheme;
    }

    public ContextThemeWrapper getWidgetHeaderThemeContext() {
        if (widgetHeaderThemeContext == null) {
            widgetHeaderThemeContext = new ContextThemeWrapper(context, themeNameToResId(widgetHeaderTheme));
        }
        return widgetHeaderThemeContext;
    }

    public String getDayHeaderTheme() {
        return dayHeaderTheme;
    }

    public ContextThemeWrapper getDayHeaderThemeContext() {
        if (dayHeaderThemeContext == null) {
            dayHeaderThemeContext = new ContextThemeWrapper(context, themeNameToResId(dayHeaderTheme));
        }
        return dayHeaderThemeContext;
    }

    public String getEntryTheme() {
        return entryTheme;
    }

    public ContextThemeWrapper getEntryThemeContext() {
        if (entryThemeContext == null) {
            entryThemeContext = new ContextThemeWrapper(context, themeNameToResId(entryTheme));
        }
        return entryThemeContext;
    }

    public WidgetHeaderLayout getWidgetHeaderLayout() {
        return widgetHeaderLayout;
    }

    public String getTextSizeScale() {
        return textSizeScale;
    }

    public String getDayHeaderAlignment() {
        return dayHeaderAlignment;
    }

    public void logMe(Class tag, String message, int widgetId) {
        Log.v(tag.getSimpleName(), message + ", widgetId:" + widgetId + "\n" + toJson());
    }
}
