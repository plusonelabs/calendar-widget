package org.andstatus.todoagenda.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksContract;

/**
 * @author yvolk@yurivolkov.com
 */
public class PermissionsUtil {

    public final static String[] permissions = {Manifest.permission.READ_CALENDAR, DmfsOpenTasksContract.PERMISSION};

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
        if (isTestMode()) return true;

        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Based on
     * http://stackoverflow.com/questions/21367646/how-to-determine-if-android-application-is-started-with-junit-testing-instrument
     */
    private static boolean isTestMode() {
        try {
            Class.forName("org.andstatus.todoagenda.calendar.MockCalendarContentProvider");
            return true;
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return false;
    }
}
