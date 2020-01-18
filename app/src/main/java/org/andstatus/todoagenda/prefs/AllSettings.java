package org.andstatus.todoagenda.prefs;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.EnvironmentChangedReceiver;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.provider.WidgetData;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.andstatus.todoagenda.AppWidgetProvider.getWidgetIds;
import static org.andstatus.todoagenda.prefs.SettingsStorage.loadJsonFromFile;

/**
 * Singleton holder of settings for all widgets
 * @author yvolk@yurivolkov.com
 */
public class AllSettings {
    private static final String TAG = AllSettings.class.getSimpleName();
    private static volatile boolean instancesLoaded = false;
    private static final Map<Integer, InstanceSettings> instances = new ConcurrentHashMap<>();

    @NonNull
    public static InstanceSettings instanceFromId(Context context, Integer widgetId) {
        ensureLoadedFromFiles(context, false);
        synchronized (instances) {
            InstanceSettings settings = instances.get(widgetId);
            return settings == null ? newInstance(context, widgetId) : settings;
        }
    }

    @NonNull
    private static InstanceSettings newInstance(Context context, Integer widgetId) {
        synchronized (instances) {
            InstanceSettings settings = instances.get(widgetId);
            if (settings == null) {
                if (widgetId != 0 && ApplicationPreferences.getWidgetId(context) == widgetId) {
                    settings = InstanceSettings.fromApplicationPreferences(context, widgetId, null);
                } else {
                    settings = new InstanceSettings(context, widgetId, "");
                }
                if (save(TAG, "newInstance", settings)) {
                    EventProviderType.initialize(context, true);
                    EnvironmentChangedReceiver.registerReceivers(instances);
                    EnvironmentChangedReceiver.updateWidget(context, widgetId);
                }
            }
            return settings;
        }
    }

    public static void ensureLoadedFromFiles(Context context, boolean reInitialize) {
        if (instancesLoaded && !reInitialize) {
            return;
        }
        synchronized (instances) {
            if (!instancesLoaded || reInitialize) {
                instances.clear();
                EventProviderType.initialize(context, reInitialize);
                for (int widgetId : getWidgetIds(context)) {
                    InstanceSettings settings;
                    try {
                        JSONObject json = loadJsonFromFile(context, getStorageKey(widgetId));
                        settings = InstanceSettings.fromJson(context, instances.get(widgetId), json);
                        if (settings.widgetId == 0) {
                            newInstance(context, widgetId);
                        } else {
                            settings.logMe(TAG, "ensureLoadedFromFiles put", widgetId);
                            instances.put(widgetId, settings);
                        }
                    } catch (Exception e) { // Starting from API21 android.system.ErrnoException may be thrown
                        Log.e("loadInstances", "widgetId:" + widgetId, e);
                        newInstance(context, widgetId);
                    }
                }
                instancesLoaded = true;
                EnvironmentChangedReceiver.registerReceivers(instances);
            }
        }
    }

    public static void addNew(String tag, Context context, InstanceSettings settings) {
         save(tag, "addNew", settings);
    }

    /** @return true if success */
    private static boolean save(String tag, String method, InstanceSettings settings) {
        if (settings.isEmpty()) {
            settings.logMe(tag, "Skipped save empty from " + method, settings.widgetId);
        } else if (settings.save(tag, method)) {
            instances.put(settings.widgetId, settings);
            return true;
        }
        return false;
    }

    public static void saveFromApplicationPreferences(Context context, Integer widgetId) {
        if (widgetId == 0) return;

        InstanceSettings settingsStored = instanceFromId(context, widgetId);
        InstanceSettings settings = InstanceSettings.fromApplicationPreferences(context, widgetId, settingsStored);
        if (settings.widgetId == widgetId && !settings.equals(settingsStored)) {
            save(TAG, "ApplicationPreferences", settings);
        }
        EnvironmentChangedReceiver.registerReceivers(instances);
    }

    @NonNull
    public static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    public static void delete(Context context, int widgetId) {
        ensureLoadedFromFiles(context, false);
        synchronized (instances) {
            instances.remove(widgetId);
            SettingsStorage.delete(context, getStorageKey(widgetId));
            if (ApplicationPreferences.getWidgetId(context) == widgetId) {
                ApplicationPreferences.setWidgetId(context, 0);
            }
        }
    }

    public static String uniqueInstanceName(Context context, int widgetId, String proposedInstanceName) {
        if (proposedInstanceName != null && proposedInstanceName.trim().length() > 0 &&
          !existsInstanceName(widgetId, proposedInstanceName)) {
            return proposedInstanceName;
        }

        String nameByWidgetId = defaultInstanceName(context, widgetId);
        if (!existsInstanceName(widgetId, nameByWidgetId)) {
            return nameByWidgetId;
        }

        int index = 1;
        String name;
        do {
            name = defaultInstanceName(context, index);
            index = index + 1;
        } while (existsInstanceName(widgetId, name));
        return name;
    }

    private static String defaultInstanceName(Context context, int index) {
        return context.getText(R.string.app_name) + " " + index;
    }

    private static boolean existsInstanceName(int widgetId, String name) {
        for (InstanceSettings settings : instances.values()) {
            if (settings.getWidgetId() != widgetId && settings.getWidgetInstanceName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static Map<Integer, InstanceSettings> getInstances(Context context) {
        ensureLoadedFromFiles(context, false);
        return instances;
    }

    public static Map<Integer, InstanceSettings> getLoadedInstances() {
        return instances;
    }

    public static void forget() {
        synchronized (instances) {
            instances.clear();
            instancesLoaded = false;
        }
    }

    public static InstanceSettings restoreWidgetSettings(Activity activity, JSONObject json, int targetWidgetId) {
        InstanceSettings settings = WidgetData.fromJson(json)
                .getSettingsForWidget(activity, instances.get(targetWidgetId), targetWidgetId);
        if (settings.hasResults()) {
            settings.clock().setSnapshotMode(SnapshotMode.SNAPSHOT_TIME);
        }
        save(TAG, "restoreWidgetSettings", settings);
        return settings;
    }
}
