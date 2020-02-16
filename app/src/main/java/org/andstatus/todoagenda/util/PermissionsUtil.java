package org.andstatus.todoagenda.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProviderType;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

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

    private final static Set<String> grantedPermissions = new ConcurrentSkipListSet<>();
    public static boolean isPermissionNeeded(Context context, String permission) {
        if (isTestMode() || grantedPermissions.contains(permission)) return false;
        boolean granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        if (granted) grantedPermissions.add(permission);
        return !granted;
    }

    private static LazyVal<Boolean> isTestMode = LazyVal.of(() -> {
        /*
         * Based on
         * http://stackoverflow.com/questions/21367646/how-to-determine-if-android-application-is-started-with-junit-testing-instrument
         */
        try {
            Class.forName("org.andstatus.todoagenda.provider.MockCalendarContentProvider");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    });

    public static boolean isTestMode() {
        return isTestMode.get();
    }
}
