package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.andstatus.todoagenda.EventAppWidgetProvider.getWidgetIds;
import static org.andstatus.todoagenda.prefs.SettingsStorage.loadJson;

/**
 * Singleton holder of settings for all widgets
 * @author yvolk@yurivolkov.com
 */
public class AllSettings {
    private static volatile boolean instancesLoaded = false;
    private static final Map<Integer, InstanceSettings> instances = new ConcurrentHashMap<>();

    @NonNull
    public static InstanceSettings instanceFromId(Context context, Integer widgetId) {
        ensureLoadedFromFiles(context);
        InstanceSettings settings = instances.get(widgetId);
        return settings == null ? newInstance(context, widgetId) : settings;
    }

    @NonNull
    private static InstanceSettings newInstance(Context context, Integer widgetId) {
        synchronized (instances) {
            InstanceSettings settings = instances.get(widgetId);
            if (settings == null) {
                if (widgetId != 0 &&
                        (ApplicationPreferences.getWidgetId(context) == widgetId || instances.isEmpty())) {
                    if (ApplicationPreferences.getWidgetId(context) != widgetId) {
                        ApplicationPreferences.setWidgetId(context, widgetId);
                    }
                    settings = InstanceSettings.fromApplicationPreferences(context, widgetId);
                } else {
                    settings = new InstanceSettings(context, widgetId, uniqueInstanceName(context, widgetId));
                }
                instances.put(widgetId, settings);
            }
            return settings;
        }
    }

    public static void ensureLoadedFromFiles(Context context) {
        if (instancesLoaded) {
            return;
        }
        synchronized (instances) {
            if (!instancesLoaded) {
                for (int widgetId : getWidgetIds(context)) {
                    InstanceSettings settings;
                    try {
                        settings = InstanceSettings.fromJson(context, loadJson(context, getStorageKey(widgetId)));
                        instances.put(widgetId, settings);
                    } catch (Exception e) { // Starting from API21 android.system.ErrnoException may be thrown
                        Log.e("loadInstances", "widgetId:" + widgetId, e);
                        newInstance(context, widgetId);
                    }
                }
                instancesLoaded = true;
                EventProviderType.initialize(context, false);
            }
        }
    }

    public static void loadFromTestData(Context context, JSONArray jsonArray) throws JSONException {
        synchronized (instances) {
            instances.clear();
            for (int index = 0; index < jsonArray.length(); index++) {
                JSONObject json = jsonArray.optJSONObject(index);
                if (json != null) {
                    InstanceSettings settings = InstanceSettings.fromJson(context, json);
                    if (settings.getWidgetId() != 0) {
                        instances.put(settings.widgetId, settings);
                    }
                }
            }
            instancesLoaded = true;
            EventProviderType.initialize(context, true);
        }
    }

    public static void saveFromApplicationPreferences(Context context, Integer widgetId) {
        if (widgetId == 0) {
            return;
        }
        InstanceSettings settings = InstanceSettings.fromApplicationPreferences(context, widgetId);
        InstanceSettings settingStored = instanceFromId(context, widgetId);
        if (settings.widgetId == widgetId && !settings.equals(settingStored)) {
            settings.save();
            instances.put(widgetId, settings);
        }
    }

    public static JSONArray toJson(Context context) {
        ensureLoadedFromFiles(context);
        return new JSONArray(instances.values());
    }

    @NonNull
    private static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    public static void delete(Context context, int widgetId) {
        ensureLoadedFromFiles(context);
        synchronized (instances) {
            instances.remove(widgetId);
            SettingsStorage.delete(context, getStorageKey(widgetId));
            if (ApplicationPreferences.getWidgetId(context) == widgetId) {
                ApplicationPreferences.setWidgetId(context, 0);
            }
        }
    }

    private static String uniqueInstanceName(Context context, int widgetId) {
        int index = instances.size();
        String name;
        do {
            index = index + 1;
            name = defaultInstanceName(context, index);
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
        ensureLoadedFromFiles(context);
        return instances;
    }
}
