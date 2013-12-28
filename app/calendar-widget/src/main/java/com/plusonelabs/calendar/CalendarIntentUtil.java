package com.plusonelabs.calendar;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import org.joda.time.DateTime;

public class CalendarIntentUtil {

	private static final String KEY_DETAIL_VIEW = "DETAIL_VIEW";
	private static final String TIME = "time";

	static Intent createOpenCalendarAtDayIntent(DateTime goToTime) {
		Intent launchIntent = createCalendarIntent();
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath(TIME);
		if (goToTime.getMillis() != 0) {
			launchIntent.putExtra(KEY_DETAIL_VIEW, true);
			ContentUris.appendId(builder, goToTime.getMillis());
		}
		launchIntent.setData(builder.build());
		return launchIntent;
	}

	static PendingIntent createOpenCalendarEventPendingIntent(Context context) {
		Intent intent = createCalendarIntent();
		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static Intent createOpenCalendarEventIntent(int eventId, DateTime from, DateTime to) {
		Intent intent = createCalendarIntent();
		intent.setData(ContentUris.withAppendedId(Events.CONTENT_URI, eventId));
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, from.getMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, to.getMillis());
		return intent;
	}

	private static Intent createCalendarIntent() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	public static Intent createNewEventIntent() {
		DateTime beginTime = new DateTime().plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
				.withMillisOfSecond(0);
		DateTime endTime = beginTime.plusHours(1);
        return new Intent(Intent.ACTION_INSERT).setData(Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getMillis());
	}
}
