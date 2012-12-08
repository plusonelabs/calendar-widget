package com.plusonelabs.calendar;

import org.joda.time.DateTime;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

public class CalendarIntentUtil {

	private static final String INTENT_TYPE_EVENT = "vnd.android.cursor.item/event";
	private static final String KEY_DETAIL_VIEW = "DETAIL_VIEW";
	private static final String TIME = "time";

	static Intent createOpenCalendarAtDayIntent(Context context, DateTime goToTime) {
		Intent launchIntent = createCalendarIntent(Intent.ACTION_VIEW);
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
		Intent intent = createCalendarIntent(Intent.ACTION_VIEW);
		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static Intent createOpenCalendarEventIntent(int eventId, DateTime from, DateTime to) {
		Intent intent = createCalendarIntent(Intent.ACTION_VIEW);
		intent.setData(ContentUris.withAppendedId(Events.CONTENT_URI, eventId));
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, from.getMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, to.getMillis());
		return intent;
	}

	private static Intent createCalendarIntent(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	public static PendingIntent createNewEventPendingIntent(Context context) {
		Intent intent = createNewEventIntent();
		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static Intent createNewEventIntent() {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType(INTENT_TYPE_EVENT);
		return intent;
	}
}
