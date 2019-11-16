package org.andstatus.todoagenda.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProviderType;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * @author yvolk@yurivolkov.com
 */
public class PermissionsUtil {

    private PermissionsUtil() {
        // Empty
    }

    @NonNull
    public static PendingIntent getPermittedPendingBroadcastIntent(InstanceSettings settings, Intent intent) {
        // We need unique request codes for each widget
        int requestCode = (intent.getAction() == null ? 1 : intent.getAction().hashCode()) + settings.getWidgetId();
        return arePermissionsGranted(settings.getContext())
                ? PendingIntent.getBroadcast(settings.getContext(), requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
                : getNoPermissionsPendingIntent(settings);
    }

    public static PendingIntent getNoPermissionsPendingIntent(InstanceSettings settings) {
        return PendingIntent.getActivity(settings.getContext(), settings.getWidgetId(),
        MainActivity.intentToStartMe(settings.getContext()), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static PendingIntent getPermittedPendingActivityIntent(InstanceSettings settings, Intent intent) {
        Intent intentPermitted = getPermittedActivityIntent(settings.getContext(), intent);
        return PendingIntent.getActivity(settings.getContext(), settings.getWidgetId(), intentPermitted, PendingIntent
                .FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static Intent getPermittedActivityIntent(@NonNull Context context, @NonNull Intent intent) {
        return arePermissionsGranted(context) ? intent : MainActivity.intentToStartMe(context);
    }

    public static boolean arePermissionsGranted(Context context) {
        AllSettings.ensureLoadedFromFiles(context, false);
        for (String permission: EventProviderType.getNeededPermissions()) {
            if (isPermissionNeeded(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPermissionNeeded(Context context, String permission) {
        return !isTestMode() &&
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }

    private static volatile Boolean isTestMode = null;
    /**
     * Based on
     * http://stackoverflow.com/questions/21367646/how-to-determine-if-android-application-is-started-with-junit-testing-instrument
     */
    public static boolean isTestMode() {
        if (isTestMode == null) {
            try {
                Class.forName("org.andstatus.todoagenda.provider.MockCalendarContentProvider");
                isTestMode = true;
            } catch (ClassNotFoundException e) {
                isTestMode = false;
            }
        }
        return isTestMode;
    }
}
