package com.plusonelabs.calendar.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.plusonelabs.calendar.EventRemoteViewsFactory;
import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.prefs.CalendarPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yvolk@yurivolkov.com
 */
public class CalendarQueryResultsStorage {
    private static final String TAG = CalendarQueryResultsStorage.class.getSimpleName();
    private static final String KEY_RESULTS_VERSION = "resultsVersion";
    private static final int RESULTS_VERSION = 1;
    private static final String KEY_RESULTS = "results";
    private static final String KEY_APP_VERSION_NAME = "versionName";
    private static final String KEY_APP_VERSION_CODE = "versionCode";
    private static final String KEY_APP_INFO = "applicationInfo";
    private static final String KEY_PREFERENCES = "preferences";

    private static final String KEY_DEVICE_INFO = "deviceInfo";
    private static final String KEY_ANDROID_VERSION_CODE = "versionCode";
    private static final String KEY_ANDROID_VERSION_RELEASE = "versionRelease";
    private static final String KEY_ANDROID_VERSION_CODENAME = "versionCodename";
    private static final String KEY_ANDROID_MANUFACTURE = "buildManufacturer";
    private static final String KEY_ANDROID_BRAND = "buildBrand";
    private static final String KEY_ANDROID_MODEL = "buildModel";

    private static volatile CalendarQueryResultsStorage theStorage = null;

    private final List<CalendarQueryResult> results = new CopyOnWriteArrayList<>();

    public static boolean store(CalendarQueryResult result) {
        CalendarQueryResultsStorage storage = theStorage;
        if (storage != null) {
            storage.results.add(result);
            return (storage == theStorage);
        }
        return false;
    }

    public static void shareEventsForDebugging(Context context) {
        final String method = "shareEventsForDebugging";
        try {
            Log.i(TAG, method + " started");
            setNeedToStoreResults(true);
            EventRemoteViewsFactory factory = new EventRemoteViewsFactory(context);
            factory.onDataSetChanged();
            String results = theStorage.getResultsAsString(context);
            if (TextUtils.isEmpty(results)) {
                Log.i(TAG, method + "; Nothing to share");
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, TAG);
                intent.putExtra(Intent.EXTRA_TEXT, results);
                context.startActivity(
                        Intent.createChooser(intent, context.getText(R.string.share_events_for_debugging_title)));
                Log.i(TAG, method + "; Shared " + results);
            }
        } finally {
            setNeedToStoreResults(false);
        }
    }

    public static boolean getNeedToStoreResults() {
        return theStorage != null;
    }

    public static void setNeedToStoreResults(boolean needToStoreResults) {
        if (needToStoreResults) {
            theStorage = new CalendarQueryResultsStorage();
        } else {
            theStorage = null;
        }
    }

    public static CalendarQueryResultsStorage getStorage() {
        return theStorage;
    }

    public List<CalendarQueryResult> getResults() {
        return results;
    }

    private String getResultsAsString(Context context) {
        try {
            return toJson(context).toString(2);
        } catch (JSONException e) {
            return "Error while formatting data " + e;
        }
    }

    public JSONObject toJson(Context context) throws JSONException {
        JSONObject jso = new JSONObject();
        List<CalendarQueryResult> results = this.results;
        jso.put(KEY_RESULTS_VERSION, RESULTS_VERSION);
        jso.put(KEY_DEVICE_INFO, getDeviceInfo());
        jso.put(KEY_APP_INFO, getAppInfo(context));
        if (results != null) {
            JSONArray jsa = new JSONArray();
            for(CalendarQueryResult result : results) {
                jsa.put(result.toJson());
            }
            jso.put(KEY_RESULTS, jsa);
        }
        return jso;
    }

    public static CalendarQueryResultsStorage fromJsonString(String jsonString) throws JSONException {
        return fromJson(new JSONObject(jsonString));
    }

    public static CalendarQueryResultsStorage fromJson(JSONObject jso) throws JSONException {
        CalendarQueryResultsStorage results = new CalendarQueryResultsStorage();
        JSONArray jsonResults = jso.getJSONArray(KEY_RESULTS);
        for (int ind=0; ind < jsonResults.length(); ind++) {
            results.results.add(CalendarQueryResult.fromJson(jsonResults.getJSONObject(ind)));
        }
        return results;
    }

    private static JSONObject getAppInfo(Context context) throws JSONException{
        JSONObject jso = new JSONObject();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
            jso.put(KEY_APP_VERSION_NAME, pi.versionName);
            jso.put(KEY_APP_VERSION_CODE, pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            jso.put(KEY_APP_VERSION_NAME, "Unable to obtain package information " + e);
            jso.put(KEY_APP_VERSION_CODE, -1);
        }
        jso.put(KEY_PREFERENCES, CalendarPreferences.toJson(context));
        return jso;
    }

    private static JSONObject getDeviceInfo() throws JSONException {
        JSONObject jso = new JSONObject();
        jso.put(KEY_ANDROID_VERSION_CODE, Build.VERSION.SDK_INT);
        jso.put(KEY_ANDROID_VERSION_RELEASE, Build.VERSION.RELEASE);
        jso.put(KEY_ANDROID_VERSION_CODENAME, Build.VERSION.CODENAME);
        jso.put(KEY_ANDROID_MANUFACTURE, Build.MANUFACTURER);
        jso.put(KEY_ANDROID_BRAND, Build.BRAND);
        jso.put(KEY_ANDROID_MODEL, Build.MODEL);
        return jso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarQueryResultsStorage results = (CalendarQueryResultsStorage) o;

        if (this.results.size() != results.results.size()) {
            return false;
        }
        for (int ind=0; ind < this.results.size(); ind++) {
            if (!this.results.get(ind).equals(results.results.get(ind))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int ind=0; ind < results.size(); ind++) {
            result = 31 * result + results.get(ind).hashCode();
        }
        return result;
    }
}
