package com.plusonelabs.calendar;

import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.prefs.ICalendarPreferences;

public class CalenderAppWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		AlarmReceiver.scheduleAlarm(context);

		for (int i = 0; i < appWidgetIds.length; i++) {
			int widgetId = appWidgetIds[i];

			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

			Intent intent = new Intent(context, CalendarWidgetService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			rv.setRemoteAdapter(R.id.event_list, intent);

			rv.setPendingIntentTemplate(R.id.event_list,
					CalendarIntentUtil.createOpenCalendarEventPendingIntent(context));

			intent = CalendarIntentUtil.createOpenCalendarAtDayIntent(context,
					System.currentTimeMillis());
			rv.setOnClickFillInIntent(R.id.empty_event_list, intent);

			Date curDate = new Date();
			String formattedDate = CalendarRemoteViewsFactory.dayStringFormatter.format(curDate)
					+ CalendarRemoteViewsFactory.dayDateFormatter.format(curDate);
			rv.setTextViewText(R.id.calendar_current_date, formattedDate.toUpperCase());

			Intent startConfigIntent = new Intent(context, CalendarConfigurationActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startConfigIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.overflow_menu, pendingIntent);

			rv.setEmptyView(R.id.event_list, R.id.empty_event_list);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (prefs.getBoolean(ICalendarPreferences.PREF_SHOW_HEADER, true)) {
				rv.setViewVisibility(R.id.action_bar, View.VISIBLE);
			} else {
				rv.setViewVisibility(R.id.action_bar, View.GONE);
			}

			appWidgetManager.updateAppWidget(widgetId, rv);
		}
	}

	public static void updateEventList(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName compName = new ComponentName(context, CalenderAppWidgetProvider.class);
		int[] widgetIds = appWidgetManager.getAppWidgetIds(compName);
		appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.event_list);
	}

	public static void updateAllWidgets(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName compName = new ComponentName(context, CalenderAppWidgetProvider.class);
		Intent intent = new Intent(context, CalenderAppWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetManager.getAppWidgetIds(compName));
		context.sendBroadcast(intent);
	}

	public static void updateWidget(Context context, int appWidgetId) {
		Intent intent = new Intent(context, CalenderAppWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		context.sendBroadcast(intent);
	}

}
