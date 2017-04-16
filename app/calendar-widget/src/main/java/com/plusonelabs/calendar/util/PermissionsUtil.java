package com.plusonelabs.calendar.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.plusonelabs.calendar.MainActivity;
import com.plusonelabs.calendar.prefs.InstanceSettings;

/**
 * @author yvolk@yurivolkov.com
 */
public class PermissionsUtil {

    public final static String PERMISSION = Manifest.permission.READ_CALENDAR;

    private PermissionsUtil() {
        // Empty
    }

    @NonNull
    public static PendingIntent getPermittedPendingIntent(InstanceSettings settings, Intent intent) {
        Intent intentPermitted = getPermittedIntent(settings.getContext(), intent);
        return PendingIntent.getActivity(settings.getContext(), settings.getWidgetId(), intentPermitted, PendingIntent
                .FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static Intent getPermittedIntent(@NonNull Context context, @NonNull Intent intent) {
        return arePermissionsGranted(context) ? intent : MainActivity.intentToStartMe(context);
    }

    public static boolean arePermissionsGranted(Context context) {
        return isTestMode() || ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Based on
     * http://stackoverflow.com/questions/21367646/how-to-determine-if-android-application-is-started-with-junit-testing-instrument
     */
    private static boolean isTestMode() {
        try {
            Class.forName("com.plusonelabs.calendar.calendar.MockCalendarContentProvider");
            return true;
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return false;
    }
}
