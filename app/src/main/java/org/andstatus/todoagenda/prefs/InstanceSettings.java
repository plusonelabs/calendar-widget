package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;

import org.andstatus.todoagenda.Alignment;
import org.andstatus.todoagenda.EndedSomeTimeAgo;
import org.andstatus.todoagenda.TextShading;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.EventEntryLayout;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetHeaderLayout;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import static org.andstatus.todoagenda.prefs.SettingsStorage.saveJson;

/**
 * Loaded settings of one Widget
 * @author yvolk@yurivolkov.com
 */
public class InstanceSettings {
    private final Context context;

    public static final String PREF_WIDGET_ID = "widgetId";
    final int widgetId;

    // ----------------------------------------------------------------------------------
    // Layout
    static final String PREF_WIDGET_HEADER_LAYOUT = "widgetHeaderLayout";
    private WidgetHeaderLayout widgetHeaderLayout = WidgetHeaderLayout.defaultValue;
    static final String PREF_SHOW_DATE_ON_WIDGET_HEADER = "showDateOnWidgetHeader";
    private boolean showDateOnWidgetHeader = true;
    static final String PREF_SHOW_DAY_HEADERS = "showDayHeaders";
    private boolean showDayHeaders = true;
    static final String PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER = "showPastEventsUnderOneHeader";
    private boolean showPastEventsUnderOneHeader = false;
    static final String PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment";
    private static final String PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.RIGHT.name();
    private String dayHeaderAlignment = PREF_DAY_HEADER_ALIGNMENT_DEFAULT;
    static final String PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents";
    private boolean showDaysWithoutEvents = false;
    static final String PREF_EVENT_ENTRY_LAYOUT = "eventEntryLayout";
    private EventEntryLayout eventEntryLayout = EventEntryLayout.DEFAULT;
    static final String PREF_SHOW_EVENT_ICON = "showEventIcon";
    private boolean showEventIcon = true;
    static final String PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT = "showNumberOfDaysToEvent";
    private boolean showNumberOfDaysToEvent = true;
    static final String PREF_MULTILINE_TITLE = "multiline_title";
    static final boolean PREF_MULTILINE_TITLE_DEFAULT = false;
    private boolean titleMultiline = PREF_MULTILINE_TITLE_DEFAULT;

    // ----------------------------------------------------------------------------------
    // Colors
    final Map<TextShadingPref, TextShading> shadings = new ConcurrentHashMap<>();

    static final String PREF_WIDGET_HEADER_BACKGROUND_COLOR = "widgetHeaderBackgroundColor";
    @ColorInt
    static final int PREF_WIDGET_HEADER_BACKGROUND_COLOR_DEFAULT = Color.TRANSPARENT;
    private int widgetHeaderBackgroundColor = PREF_WIDGET_HEADER_BACKGROUND_COLOR_DEFAULT;
    static final String PREF_PAST_EVENTS_BACKGROUND_COLOR = "pastEventsBackgroundColor";
    @ColorInt static final int PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT = 0xBF78782C;
    private int pastEventsBackgroundColor = PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT;
    static final String PREF_TODAYS_EVENTS_BACKGROUND_COLOR = "todaysEventsBackgroundColor";
    @ColorInt static final int PREF_TODAYS_EVENTS_BACKGROUND_COLOR_DEFAULT = 0xDAFFFFFF;
    private int todaysEventsBackgroundColor = PREF_TODAYS_EVENTS_BACKGROUND_COLOR_DEFAULT;
    static final String PREF_EVENTS_BACKGROUND_COLOR = "backgroundColor";
    @ColorInt static final int PREF_EVENTS_BACKGROUND_COLOR_DEFAULT = 0x80000000;
    private int eventsBackgroundColor = PREF_EVENTS_BACKGROUND_COLOR_DEFAULT;

    // ----------------------------------------------------------------------------------
    // Event details
    static final String PREF_SHOW_END_TIME = "showEndTime";
    static final boolean PREF_SHOW_END_TIME_DEFAULT = true;
    private boolean showEndTime = PREF_SHOW_END_TIME_DEFAULT;
    static final String PREF_SHOW_LOCATION = "showLocation";
    static final boolean PREF_SHOW_LOCATION_DEFAULT = true;
    private boolean showLocation = PREF_SHOW_LOCATION_DEFAULT;
    static final String PREF_FILL_ALL_DAY = "fillAllDay";
    static final boolean PREF_FILL_ALL_DAY_DEFAULT = true;
    private boolean fillAllDayEvents = PREF_FILL_ALL_DAY_DEFAULT;
    static final String PREF_INDICATE_ALERTS = "indicateAlerts";
    private boolean indicateAlerts = true;
    static final String PREF_INDICATE_RECURRING = "indicateRecurring";
    private boolean indicateRecurring = false;

    // ----------------------------------------------------------------------------------
    // Event filters
    static final String PREF_EVENTS_ENDED = "eventsEnded";
    private EndedSomeTimeAgo eventsEnded = EndedSomeTimeAgo.NONE;
    static final String PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR = "showPastEventsWithDefaultColor";
    private boolean showPastEventsWithDefaultColor = false;
    static final String PREF_EVENT_RANGE = "eventRange";
    static final String PREF_EVENT_RANGE_DEFAULT = "30";
    private int eventRange = Integer.valueOf(PREF_EVENT_RANGE_DEFAULT);
    static final String PREF_HIDE_BASED_ON_KEYWORDS = "hideBasedOnKeywords";
    private String hideBasedOnKeywords = "";
    static final String PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT =
            "showOnlyClosestInstanceOfRecurringEvent";
    private boolean showOnlyClosestInstanceOfRecurringEvent = false;

    // ----------------------------------------------------------------------------------
    // Calendars and task lists
    static final String PREF_ACTIVE_SOURCES = "activeSources";
    private List<EventSource> activeEventSources = Collections.emptyList();

    // ----------------------------------------------------------------------------------
    // Other
    static final String PREF_WIDGET_INSTANCE_NAME = "widgetInstanceName";
    private final String widgetInstanceName;
    static final String PREF_TEXT_SIZE_SCALE = "textSizeScale";
    static final String PREF_TEXT_SIZE_SCALE_DEFAULT = "1.0";
    private String textSizeScale = PREF_TEXT_SIZE_SCALE_DEFAULT;
    static final String PREF_DATE_FORMAT = "dateFormat";
    static final String PREF_DATE_FORMAT_DEFAULT = "auto";
    private String dateFormat = PREF_DATE_FORMAT_DEFAULT;
    static final String PREF_ABBREVIATE_DATES = "abbreviateDates";
    static final boolean PREF_ABBREVIATE_DATES_DEFAULT = false;
    private boolean abbreviateDates = PREF_ABBREVIATE_DATES_DEFAULT;
    static final String PREF_LOCK_TIME_ZONE = "lockTimeZone";
    static final String PREF_LOCKED_TIME_ZONE_ID = "lockedTimeZoneId";
    private String lockedTimeZoneId = "";

    // ----------------------------------------------------------------------------------
    // Feedback
    static final String KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging";

    public static InstanceSettings fromJson(Context context, JSONObject json) throws JSONException {
        InstanceSettings settings = new InstanceSettings(context, json.optInt(PREF_WIDGET_ID),
                json.optString(PREF_WIDGET_INSTANCE_NAME));
        if (settings.widgetId == 0) {
            return settings;
        }
        if (json.has(PREF_SHOW_DATE_ON_WIDGET_HEADER)) {
            settings.showDateOnWidgetHeader = json.getBoolean(PREF_SHOW_DATE_ON_WIDGET_HEADER);
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
        for (TextShadingPref pref: TextShadingPref.values()) {
            if (json.has(pref.preferenceName)) {
                settings.shadings.put(pref,
                        TextShading.fromName(json.getString(pref.preferenceName), pref.defaultShading));
            }
        }
        if (json.has(PREF_WIDGET_HEADER_LAYOUT)) {
            settings.widgetHeaderLayout = WidgetHeaderLayout.fromValue(json.getString(PREF_WIDGET_HEADER_LAYOUT));
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
        synchronized (ApplicationPreferences.class) {
            InstanceSettings settings = new InstanceSettings(context, widgetId,
                    ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME,
                            ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME, "")));
            settings.showDateOnWidgetHeader = ApplicationPreferences.getShowDateOnWidgetHeader(context);
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
            settings.widgetHeaderLayout = ApplicationPreferences.getWidgetHeaderLayout(context);
            for (TextShadingPref pref: TextShadingPref.values()) {
                String themeName = ApplicationPreferences.getString(context, pref.preferenceName,
                        pref.defaultShading.name());
                settings.shadings.put(pref, TextShading.fromName(themeName, pref.defaultShading));
            }
            settings.textSizeScale = ApplicationPreferences.getString(context, PREF_TEXT_SIZE_SCALE,
                    PREF_TEXT_SIZE_SCALE_DEFAULT);
            settings.dayHeaderAlignment = ApplicationPreferences.getString(context, PREF_DAY_HEADER_ALIGNMENT,
                    PREF_DAY_HEADER_ALIGNMENT_DEFAULT);
            return settings;
        }
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
            json.put(PREF_SHOW_DATE_ON_WIDGET_HEADER, showDateOnWidgetHeader);
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
            for (TextShadingPref pref: TextShadingPref.values()) {
                json.put(pref.preferenceName, getShading(pref).name());
            }
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

    public TextShading getShading(TextShadingPref pref) {
        TextShading shading = shadings.get(pref);
        return shading == null ? pref.defaultShading : shading;
    }

    public int getEntryBackgroundColor(WidgetEntry<?> entry) {
        return entry.getTimeSection()
                .select(getPastEventsBackgroundColor(), getTodaysEventsBackgroundColor(), getEventsBackgroundColor());
    }

    public ContextThemeWrapper getShadingContext(TextShadingPref pref) {
        return new ContextThemeWrapper(context, getShading(pref).themeResId);
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

    public boolean getShowDateOnWidgetHeader() {
        return showDateOnWidgetHeader;
    }

    public boolean noPastEvents() {
        return !getShowPastEventsWithDefaultColor() &&
                getEventsEnded() == EndedSomeTimeAgo.NONE &&
                noTaskSources();
    }

    public boolean noTaskSources() {
        for(EventSource source: activeEventSources) {
            if (!source.providerType.isCalendar) return false;
        }
        return true;
    }
}
