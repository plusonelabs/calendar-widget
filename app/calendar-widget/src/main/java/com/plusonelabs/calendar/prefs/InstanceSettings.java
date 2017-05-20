package com.plusonelabs.calendar.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.EndedSomeTimeAgo;
import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.widget.EventEntryLayout;

import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import static com.plusonelabs.calendar.EventAppWidgetProvider.getWidgetIds;
import static com.plusonelabs.calendar.Theme.themeNameToResId;
import static com.plusonelabs.calendar.prefs.ApplicationPreferences.*;
import static com.plusonelabs.calendar.prefs.SettingsStorage.*;

/**
 * @author yvolk@yurivolkov.com
 */
public class InstanceSettings {

    private static volatile boolean instancesLoaded = false;
    private static final Map<Integer, InstanceSettings> instances = new ConcurrentHashMap<>();

    private final Context context;
    private volatile ContextThemeWrapper entryThemeContext = null;
    private volatile ContextThemeWrapper headerThemeContext = null;

    private final int widgetId;
    private final String widgetInstanceName;
    private boolean justCreated = true;
    private Set<String> activeCalendars = new HashSet<>();
    private int eventRange = Integer.valueOf(PREF_EVENT_RANGE_DEFAULT);
    private EndedSomeTimeAgo eventsEnded = EndedSomeTimeAgo.NONE;
    private boolean fillAllDayEvents = PREF_FILL_ALL_DAY_DEFAULT;
    private String hideBasedOnKeywords = "";
    private int pastEventsBackgroundColor = PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT;
    private boolean showDaysWithoutEvents = false;
    private boolean showDayHeaders = true;
    private boolean showPastEventsWithDefaultColor = false;
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
    private String entryTheme = PREF_ENTRY_THEME_DEFAULT;
    private String headerTheme = PREF_HEADER_THEME_DEFAULT;
    private boolean showWidgetHeader = true;
    private int backgroundColor = PREF_BACKGROUND_COLOR_DEFAULT;
    private String textSizeScale = PREF_TEXT_SIZE_SCALE_DEFAULT;
    private String dayHeaderAlignment = PREF_DAY_HEADER_ALIGNMENT_DEFAULT;

    @NonNull
    public static InstanceSettings fromId(Context context, Integer widgetId) {
        ensureInstancesAreLoaded(context);
        InstanceSettings settings = instances.get(widgetId);
        return settings != null ? settings : newInstance(context, widgetId);
    }

    @NonNull
    private static InstanceSettings newInstance(Context context, Integer widgetId) {
        synchronized (instances) {
            InstanceSettings settings = instances.get(widgetId);
            if (settings == null) {
                if (widgetId != 0 &&
                        (ApplicationPreferences.getWidgetId(context) == widgetId || instances.isEmpty())) {
                    settings = fromApplicationPreferences(context, widgetId);
                } else {
                    settings = new InstanceSettings(context, widgetId, "");
                }
                instances.put(widgetId, settings);
            }
            return settings;
        }
    }

    private static void ensureInstancesAreLoaded(Context context) {
        if (instancesLoaded) {
            return;
        }
        synchronized (instances) {
            if (!instancesLoaded) {
                for (int widgetId : getWidgetIds(context)) {
                    InstanceSettings settings;
                    try {
                        settings = fromJson(context, loadJson(context, getStorageKey(widgetId)));
                        instances.put(widgetId, settings);
                    } catch (Exception e) { // Starting from API21 android.system.ErrnoException may be thrown
                        Log.e("loadInstances", "widgetId:" + widgetId, e);
                        newInstance(context, widgetId);
                    }
                }
                instancesLoaded = true;
            }
        }
    }

    public static void fromJson(Context context, JSONArray jsonArray) throws JSONException {
        synchronized (instances) {
            instances.clear();
            for (int index = 0; index < jsonArray.length(); index++) {
                JSONObject json = jsonArray.optJSONObject(index);
                if (json != null) {
                    InstanceSettings settings = fromJson(context, json);
                    if (settings.getWidgetId() != 0) {
                        instances.put(settings.widgetId, settings);
                    }
                }
            }
            instancesLoaded = true;
        }
    }

    public static InstanceSettings fromJson(Context context, JSONObject json) throws JSONException {
        InstanceSettings settings = new InstanceSettings(context, json.optInt(PREF_WIDGET_ID),
                json.optString(PREF_WIDGET_INSTANCE_NAME));
        if (settings.widgetId == 0) {
            return settings;
        }
        settings.justCreated = false;
        if (json.has(PREF_ACTIVE_CALENDARS)) {
            settings.activeCalendars = jsonArray2StringSet(json.getJSONArray(PREF_ACTIVE_CALENDARS));
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
        if (json.has(PREF_PAST_EVENTS_BACKGROUND_COLOR)) {
            settings.pastEventsBackgroundColor = json.getInt(PREF_PAST_EVENTS_BACKGROUND_COLOR);
        }
        if (json.has(PREF_SHOW_DAYS_WITHOUT_EVENTS)) {
            settings.showDaysWithoutEvents = json.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS);
        }
        if (json.has(PREF_SHOW_DAY_HEADERS)) {
            settings.showDayHeaders = json.getBoolean(PREF_SHOW_DAY_HEADERS);
        }
        if (json.has(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR)) {
            settings.showPastEventsWithDefaultColor = json.getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR);
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
        if (json.has(PREF_ENTRY_THEME)) {
            settings.entryTheme = json.getString(PREF_ENTRY_THEME);
        }
        if (json.has(PREF_HEADER_THEME)) {
            settings.headerTheme = json.getString(PREF_HEADER_THEME);
        }
        if (json.has(PREF_SHOW_WIDGET_HEADER)) {
            settings.showWidgetHeader = json.getBoolean(PREF_SHOW_WIDGET_HEADER);
        }
        if (json.has(PREF_BACKGROUND_COLOR)) {
            settings.backgroundColor = json.getInt(PREF_BACKGROUND_COLOR);
        }
        if (json.has(PREF_TEXT_SIZE_SCALE)) {
            settings.textSizeScale = json.getString(PREF_TEXT_SIZE_SCALE);
        }
        if (json.has(PREF_DAY_HEADER_ALIGNMENT)) {
            settings.dayHeaderAlignment = json.getString(PREF_DAY_HEADER_ALIGNMENT);
        }
        return settings;
    }

    private static Set<String> jsonArray2StringSet(JSONArray jsonArray) {
        Set<String> set = new HashSet<>();
        for (int index = 0; index < jsonArray.length(); index++) {
            String value = jsonArray.optString(index);
            if (value != null) {
                set.add(value);
            }
        }
        return set;
    }

    public static void save(Context context, Integer widgetId) {
        if (widgetId == 0) {
            return;
        }
        InstanceSettings settings = fromApplicationPreferences(context, widgetId);
        InstanceSettings settingStored = fromId(context, widgetId);
        if (!settings.equals(settingStored)) {
            settings.save();
            instances.put(widgetId, settings);
        }
    }

    public static InstanceSettings fromApplicationPreferences(Context context, int widgetId) {
        InstanceSettings settings = new InstanceSettings(context, widgetId,
                ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME,
                        ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME, "")));
        settings.justCreated = false;
        settings.activeCalendars = ApplicationPreferences.getActiveCalendars(context);
        settings.eventRange = ApplicationPreferences.getEventRange(context);
        settings.eventsEnded = ApplicationPreferences.getEventsEnded(context);
        settings.fillAllDayEvents = ApplicationPreferences.getFillAllDayEvents(context);
        settings.hideBasedOnKeywords = ApplicationPreferences.getHideBasedOnKeywords(context);
        settings.pastEventsBackgroundColor = ApplicationPreferences.getPastEventsBackgroundColor(context);
        settings.showDaysWithoutEvents = ApplicationPreferences.getShowDaysWithoutEvents(context);
        settings.showDayHeaders = ApplicationPreferences.getShowDayHeaders(context);
        settings.showPastEventsWithDefaultColor = ApplicationPreferences.getShowPastEventsWithDefaultColor(context);
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
        settings.entryTheme = ApplicationPreferences.getString(context, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT);
        settings.headerTheme = ApplicationPreferences.getString(context, PREF_HEADER_THEME, PREF_HEADER_THEME_DEFAULT);
        settings.showWidgetHeader = ApplicationPreferences.getBoolean(context, PREF_SHOW_WIDGET_HEADER, true);
        settings.backgroundColor = ApplicationPreferences.getInt(context, PREF_BACKGROUND_COLOR,
                PREF_BACKGROUND_COLOR_DEFAULT);
        settings.textSizeScale = ApplicationPreferences.getString(context, PREF_TEXT_SIZE_SCALE,
                PREF_TEXT_SIZE_SCALE_DEFAULT);
        settings.dayHeaderAlignment = ApplicationPreferences.getString(context, PREF_DAY_HEADER_ALIGNMENT,
                PREF_DAY_HEADER_ALIGNMENT_DEFAULT);
        return settings;
    }

    public static JSONArray toJson(Context context) {
        ensureInstancesAreLoaded(context);
        return new JSONArray(instances.values());
    }

    @NonNull
    private static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    public static void delete(Context context, int widgetId) {
        ensureInstancesAreLoaded(context);
        synchronized (instances) {
            if (instances.containsKey(widgetId)) {
                instances.remove(widgetId);
            }
            SettingsStorage.delete(context, getStorageKey(widgetId));
            if (ApplicationPreferences.getWidgetId(context) == widgetId) {
                ApplicationPreferences.setWidgetId(context, 0);
            }
        }
    }

    private InstanceSettings(Context context, int widgetId, String widgetInstanceName) {
        this.context = context;
        this.widgetId = widgetId;
        this.widgetInstanceName = uniqueInstanceName(widgetInstanceName);
    }

    private String uniqueInstanceName(String widgetInstanceName) {
        int index = instances.size();
        String name = TextUtils.isEmpty(widgetInstanceName) ? defaultInstanceName(++index) : widgetInstanceName;
        while (existsInstanceName(name)) {
            name = defaultInstanceName(++index);
        }
        return name;
    }

    private String defaultInstanceName(int index) {
        return context.getText(R.string.app_name) + " " + index;
    }

    private boolean existsInstanceName(String name) {
        for (InstanceSettings settings : instances.values()) {
            if (settings.getWidgetId() != widgetId && settings.getWidgetInstanceName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void save() {
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
            json.put(PREF_ACTIVE_CALENDARS, new JSONArray(activeCalendars));
            json.put(PREF_EVENT_RANGE, eventRange);
            json.put(PREF_EVENTS_ENDED, eventsEnded.save());
            json.put(PREF_FILL_ALL_DAY, fillAllDayEvents);
            json.put(PREF_HIDE_BASED_ON_KEYWORDS, hideBasedOnKeywords);
            json.put(PREF_PAST_EVENTS_BACKGROUND_COLOR, pastEventsBackgroundColor);
            json.put(PREF_SHOW_DAYS_WITHOUT_EVENTS, showDaysWithoutEvents);
            json.put(PREF_SHOW_DAY_HEADERS, showDayHeaders);
            json.put(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, showPastEventsWithDefaultColor);
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
            json.put(PREF_ENTRY_THEME, entryTheme);
            json.put(PREF_HEADER_THEME, headerTheme);
            json.put(PREF_SHOW_WIDGET_HEADER, showWidgetHeader);
            json.put(PREF_BACKGROUND_COLOR, backgroundColor);
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

    public boolean isJustCreated() {
        return justCreated;
    }

    public Set<String> getActiveCalendars() {
        return activeCalendars;
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

    public int getPastEventsBackgroundColor() {
        return pastEventsBackgroundColor;
    }

    public boolean getShowDaysWithoutEvents() {
        return showDaysWithoutEvents;
    }

    public boolean getShowDayHeaders() {
        return showDayHeaders;
    }

    public boolean getShowPastEventsWithDefaultColor() {
        return showPastEventsWithDefaultColor;
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

    public String getHeaderTheme() {
        return headerTheme;
    }

    public ContextThemeWrapper getHeaderThemeContext() {
        if (headerThemeContext == null) {
            headerThemeContext = new ContextThemeWrapper(context, themeNameToResId(headerTheme));
        }
        return headerThemeContext;
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

    public boolean getShowWidgetHeader() {
        return showWidgetHeader;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public String getTextSizeScale() {
        return textSizeScale;
    }

    public String getDayHeaderAlignment() {
        return dayHeaderAlignment;
    }

    public static Map<Integer, InstanceSettings> getInstances(Context context) {
        ensureInstancesAreLoaded(context);
        return instances;
    }
}
