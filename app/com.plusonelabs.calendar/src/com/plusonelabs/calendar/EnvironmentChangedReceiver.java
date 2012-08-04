package com.plusonelabs.calendar;

import java.util.TimeZone;

import org.joda.time.DateTimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EnvironmentChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_LOCALE_CHANGED)
				|| action.equals(Intent.ACTION_TIME_CHANGED)
				|| action.equals(Intent.ACTION_DATE_CHANGED)
				|| action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
			DateTimeZone.setDefault(DateTimeZone.forID(TimeZone.getDefault().getID()));
			EventRemoteViewsFactory.initiDateFormatter();
			EventAppWidgetProvider.updateAllWidgets(context);
		}
		EventAppWidgetProvider.updateEventList(context);
	}
}