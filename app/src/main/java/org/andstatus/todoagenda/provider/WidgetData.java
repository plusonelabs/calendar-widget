package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yvolk@yurivolkov.com
 */
public class WidgetData {
    public static final WidgetData EMPTY = new WidgetData(new JSONObject());

    private static final String TAG = WidgetData.class.getSimpleName();
    private static final String KEY_SETTINGS = "settings";

    private static final String KEY_APP_VERSION_NAME = "versionName";
    private static final String KEY_APP_VERSION_CODE = "versionCode";
    private static final String KEY_APP_INFO = "applicationInfo";

    private static final String KEY_DEVICE_INFO = "deviceInfo";
    private static final String KEY_ANDROID_VERSION_CODE = "versionCode";
    private static final String KEY_ANDROID_VERSION_RELEASE = "versionRelease";
    private static final String KEY_ANDROID_VERSION_CODENAME = "versionCodename";
    private static final String KEY_ANDROID_MANUFACTURE = "buildManufacturer";
    private static final String KEY_ANDROID_BRAND = "buildBrand";
    private static final String KEY_ANDROID_MODEL = "buildModel";

    private final JSONObject jsonData;

    private final List<QueryResult> results = new CopyOnWriteArrayList<>();

    public static WidgetData fromJson(JSONObject jso) {
        if (jso == null) {
            return EMPTY;
        } else {
            return new WidgetData(jso);
        }
    }

    public static WidgetData fromSettings(Context context, @Nullable InstanceSettings settings) {
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_DEVICE_INFO, getDeviceInfo());
            json.put(KEY_APP_INFO, getAppInfo(context));
            if (settings != null){
                json.put(KEY_SETTINGS, settings.toJson());
            }
        } catch (JSONException e) {
            Log.w(TAG,"fromSettings failed; " + settings, e);
        }
        return new WidgetData(json);
    }

    private WidgetData(JSONObject jsonData) {
        this.jsonData = jsonData;
    }

    public List<QueryResult> getResults() {
        return results;
    }

    public String toJsonString() {
        try {
            return toJson().toString(2);
        } catch (JSONException e) {
            return TAG + " Error while formatting data " + e;
        }
    }

    JSONObject toJson() {
        return jsonData;
    }

    private static JSONObject getAppInfo(Context context) throws JSONException {
        JSONObject json = new JSONObject();
        try {
            PackageManager pm = context.getPackageManager();
            Context applicationContext = context.getApplicationContext();
            PackageInfo pi = pm.getPackageInfo(
                    (applicationContext == null ? context : applicationContext).getPackageName(), 0);
            json.put(KEY_APP_VERSION_NAME, pi.versionName);
            json.put(KEY_APP_VERSION_CODE, pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            json.put(KEY_APP_VERSION_NAME, "Unable to obtain package information " + e);
            json.put(KEY_APP_VERSION_CODE, -1);
        }
        return json;
    }

    private static JSONObject getDeviceInfo() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_ANDROID_VERSION_CODE, Build.VERSION.SDK_INT);
            json.put(KEY_ANDROID_VERSION_RELEASE, Build.VERSION.RELEASE);
            json.put(KEY_ANDROID_VERSION_CODENAME, Build.VERSION.CODENAME);
            json.put(KEY_ANDROID_MANUFACTURE, Build.MANUFACTURER);
            json.put(KEY_ANDROID_BRAND, Build.BRAND);
            json.put(KEY_ANDROID_MODEL, Build.MODEL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String toString() {
        return TAG + ":" + jsonData;
    }

    public InstanceSettings getSettingsForWidget(Context context, InstanceSettings storedSettings, int targetWidgetId) {
        JSONObject jsonSettings = jsonData.optJSONObject(KEY_SETTINGS);
        if (jsonSettings == null) return InstanceSettings.EMPTY;

        InstanceSettings originalSettings = InstanceSettings.fromJson(context, storedSettings, jsonSettings);
        InstanceSettings targetSettings = originalSettings.asForWidget(context, targetWidgetId);
        QueryResultsStorage results = QueryResultsStorage.fromJson(targetWidgetId, jsonData);
        if (!results.getResults().isEmpty()) {
            targetSettings.setResultsStorage(results);
        }
        return targetSettings;
    }

}
