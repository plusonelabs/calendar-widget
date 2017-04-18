package com.plusonelabs.calendar;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import com.plusonelabs.calendar.prefs.InstanceSettings;
import com.plusonelabs.calendar.util.PermissionsUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class CalendarIntentUtil {

    private static final String KEY_DETAIL_VIEW = "DETAIL_VIEW";
    private static final String TIME = "time";

    static Intent createOpenCalendarAtDayIntent(DateTime goToTime) {
        Intent intent = createCalendarIntent();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath(TIME);
        if (goToTime.getMillis() != 0) {
            intent.putExtra(KEY_DETAIL_VIEW, true);
            ContentUris.appendId(builder, goToTime.getMillis());
        }
        intent.setData(builder.build());
        return intent;
    }

    static PendingIntent createOpenCalendarEventPendingIntent(InstanceSettings settings) {
        return PermissionsUtil.getPermittedPendingIntent(settings, createCalendarIntent());
    }

    static PendingIntent createOpenCalendarPendingIntent(InstanceSettings settings) {
        return PermissionsUtil.getPermittedPendingIntent(settings,
                createOpenCalendarAtDayIntent(new DateTime(settings.getTimeZone())));
    }

    public static Intent createCalendarIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent createNewEventIntent(DateTimeZone timeZone) {
        DateTime beginTime = new DateTime(timeZone).plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
                .withMillisOfSecond(0);
        DateTime endTime = beginTime.plusHours(1);
        return new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getMillis());
    }
}
