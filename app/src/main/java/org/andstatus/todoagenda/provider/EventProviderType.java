package org.andstatus.todoagenda.provider;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.andstatus.todoagenda.calendar.CalendarEventProvider;
import org.andstatus.todoagenda.calendar.CalendarEventVisualizer;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.task.TaskVisualizer;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksContract;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;
import org.andstatus.todoagenda.task.samsung.SamsungTasksProvider;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/** All supported Event providers */
public enum EventProviderType {
    EMPTY(0, true, ""),
    CALENDAR(1, true, Manifest.permission.READ_CALENDAR) {
        @Override
        public EventProvider getEventProvider(Context context, int widgetId) {
            return new CalendarEventProvider(this, context, widgetId);
        }
    },
    DMFS_OPEN_TASKS(2, false, DmfsOpenTasksContract.PERMISSION) {
        @Override
        public EventProvider getEventProvider(Context context, int widgetId) {
            return new DmfsOpenTasksProvider(this, context, widgetId);
        }
    },
    SAMSUNG_TASKS(3, false, Manifest.permission.READ_CALENDAR) {
        @Override
        public EventProvider getEventProvider(Context context, int widgetId) {
            return new SamsungTasksProvider(this, context, widgetId);
        }
    };

    public final int id;
    public final boolean isCalendar;
    public final String permission;

    private static List<EventProvider> providers = new CopyOnWriteArrayList<>();
    private static List<EventSource> sources = new CopyOnWriteArrayList<>();
    private static Set<String> permissionsNeeded = new CopyOnWriteArraySet<>();
    private static volatile boolean initialized = false;

    EventProviderType(int id, boolean isCalendar, String permission) {
        this.id = id;
        this.isCalendar = isCalendar;
        this.permission = permission;
    }

    public static void initialize(Context context, boolean reInitialize) {
        if (initialized && !reInitialize) return;

        providers.clear();
        sources.clear();
        for(EventProviderType type : EventProviderType.values()) {
            EventProvider provider = type.getEventProvider(context, 0);
            providers.add(provider);
            Collection<EventSource> ss = Collections.emptyList();
            boolean permissionNeeded = false;
            try {
                ss = provider.fetchAvailableSources();
                sources.addAll(ss);
            } catch (SecurityException e) {
                Log.i(EventProviderType.class.getSimpleName(), "initialize: " + e.getMessage());
                permissionNeeded = true;
            } catch (Exception e) {
                Log.i(EventProviderType.class.getSimpleName(), "initialize: " + e.getMessage(), e);
            }
            Log.i(EventProviderType.class.getSimpleName(), "provider " + type +
                    ", " + (ss.isEmpty() ? "no" : ss.size()) + " sources" +
                    (permissionNeeded ? ", needs " + type.permission : ""));
            if (permissionNeeded) {
                permissionsNeeded.add(type.permission);
            }
        }
        initialized = true;
    }

    @NonNull
    public static EventProviderType fromId(int id) {
        for(EventProviderType type : EventProviderType.values()) {
            if(type.id == id) return type;
        }
        return EMPTY;
    }

    @NonNull
    public static Set<String> getNeededPermissions(Context context) {
        initialize(context, false);
        return permissionsNeeded;
    }

    public static List<EventSource> getAvailableSources() {
        return sources;
    }

    public EventProvider getEventProvider(Context context, int widgetId) {
        return new EventProvider(this, context, widgetId);
    }

    public WidgetEntryVisualizer<? extends WidgetEntry> getVisualizer(Context context, int widgetId) {
        EventProvider eventProvider = getEventProvider(context, widgetId);
        return isCalendar
            ? new CalendarEventVisualizer(eventProvider)
            : new TaskVisualizer(eventProvider);
    }

    public boolean hasEventSources() {
        for(EventSource source: sources) {
            if (source.providerType == this) return true;
        }
        return false;
    }}
