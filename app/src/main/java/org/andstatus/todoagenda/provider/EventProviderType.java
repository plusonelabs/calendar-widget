package org.andstatus.todoagenda.provider;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.EnvironmentChangedReceiver;
import org.andstatus.todoagenda.calendar.CalendarEventProvider;
import org.andstatus.todoagenda.calendar.CalendarEventVisualizer;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.task.TaskVisualizer;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksContract;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;
import org.andstatus.todoagenda.task.samsung.SamsungTasksProvider;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/** All supported Event providers */
public enum EventProviderType {
    EMPTY(0, true, "", ""),
    CALENDAR(1, true, Manifest.permission.READ_CALENDAR, "com.android.calendar") {
        @Override
        public EventProvider getEventProvider(Context context, int widgetId) {
            return new CalendarEventProvider(this, context, widgetId);
        }
    },
    DMFS_OPEN_TASKS(2, false, DmfsOpenTasksContract.PERMISSION, "org.dmfs.tasks") {
        @Override
        public EventProvider getEventProvider(Context context, int widgetId) {
            return new DmfsOpenTasksProvider(this, context, widgetId);
        }
    },
    SAMSUNG_TASKS(3, false, Manifest.permission.READ_CALENDAR, "com.android.calendar") {
        @Override
        public EventProvider getEventProvider(Context context, int widgetId) {
            return new SamsungTasksProvider(this, context, widgetId);
        }
    };

    private static final String TAG = EventProviderType.class.getSimpleName();
    public final int id;
    public final boolean isCalendar;
    public final String permission;
    private final String authority;

    private static List<OrderedEventSource> sources = new CopyOnWriteArrayList<>();
    private static Set<String> permissionsNeeded = new CopyOnWriteArraySet<>();
    private static volatile boolean initialized = false;

    EventProviderType(int id, boolean isCalendar, String permission, String authority) {
        this.id = id;
        this.isCalendar = isCalendar;
        this.permission = permission;
        this.authority = authority;
    }

    public static void initialize(Context context, boolean reInitialize) {
        if (initialized && !reInitialize) return;

        sources.clear();
        for(EventProviderType type : EventProviderType.values()) {
            EventProvider provider = type.getEventProvider(context, 0);
            provider.fetchAvailableSources()
            .onSuccess( ss -> {
                Log.i(TAG, "provider " + type + ", " + (ss.isEmpty() ? "no" : ss.size()) + " sources");
                sources.addAll(OrderedEventSource.fromSources(ss));
            })
            .onFailure(e -> {
                Log.i(TAG, "provider " + type + " initialization error: " + e.getMessage());
                if (e instanceof SecurityException) {
                    Log.i(TAG, "provider " + type + ", needs " + type.permission);
                    permissionsNeeded.add(type.permission);
                }
            });
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
    public static Set<String> getNeededPermissions() {
        return permissionsNeeded;
    }

    public static List<OrderedEventSource> getAvailableSources() {
        return sources;
    }

    public static void forget() {
        sources.clear();
        permissionsNeeded.clear();
        initialized = false;
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
        for(OrderedEventSource orderedSource: sources) {
            if (orderedSource.source.providerType == this) return true;
        }
        return false;
    }

    public static void registerProviderChangedReceivers(Context context, EnvironmentChangedReceiver receiver) {
        Set<String> registeredAuthorities = new HashSet<>();
        for(EventProviderType type : EventProviderType.values()) {
            String authority = type.authority;
            if (type.hasEventSources() && authority.length() > 0 && !registeredAuthorities.contains(authority)) {
                registeredAuthorities.add(authority);
                registerProviderChangedReceiver(context, receiver, authority);
            }
        }
    }

    private static void registerProviderChangedReceiver(Context context, EnvironmentChangedReceiver receiver,
                                                        String authority) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PROVIDER_CHANGED");
        intentFilter.addDataScheme("content");
        intentFilter.addDataAuthority(authority, null);
        context.registerReceiver(receiver, intentFilter);
    }

}
