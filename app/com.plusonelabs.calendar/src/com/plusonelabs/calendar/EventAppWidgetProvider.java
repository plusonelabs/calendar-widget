package com.plusonelabs.calendar;

import static com.plusonelabs.calendar.CalendarIntentUtil.*;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.*;

import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.prefs.CalendarPreferences;

public class EventAppWidgetProvider extends AppWidgetProvider {

	private static final String METHOD_SET_BACKGROUND_RESOURCE = "setBackgroundResource";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		AlarmReceiver.scheduleAlarm(context);
		for (int i = 0; i < appWidgetIds.length; i++) {
			int widgetId = appWidgetIds[i];
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
			configureBackground(context, rv);
			configureActionBar(context, rv);
			configureList(context, widgetId, rv);
			appWidgetManager.updateAppWidget(widgetId, rv);
		}
	}

	public void configureBackground(Context context, RemoteViews rv) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean(CalendarPreferences.PREF_SHOW_HEADER, true)) {
			rv.setViewVisibility(R.id.action_bar, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.action_bar, View.GONE);
		}
		int bgTrans = prefs.getInt(CalendarPreferences.PREF_BACKGROUND_TRANSPARENCY,
				PREF_BACKGROUND_TRANSPARENCY_DEFAULT);
		rv.setInt(R.id.widget_background, METHOD_SET_BACKGROUND_RESOURCE,
				transparencyToDrawableRes(bgTrans));
	}

	public void configureActionBar(Context context, RemoteViews rv) {
		String formattedDate = DateUtils.formatDateTime(context, System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
		rv.setTextViewText(R.id.calendar_current_date,
				formattedDate.toUpperCase(Locale.getDefault()));
		Intent startConfigIntent = new Intent(context, WidgetConfigurationActivity.class);
		PendingIntent menuPendingIntent = PendingIntent.getActivity(context, 0, startConfigIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.overflow_menu, menuPendingIntent);
		Intent intent = CalendarIntentUtil.createNewEventIntent();
		if (isIntentAvailable(context, intent)) {
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.add_event, pendingIntent);
		} else {
			rv.setViewVisibility(R.id.add_event, View.GONE);
		}
	}

	public static boolean isIntentAvailable(Context context, Intent intent) {
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		System.out.println(list);
		return list.size() > 0;
	}

	public void configureList(Context context, int widgetId, RemoteViews rv) {
		Intent intent = new Intent(context, EventWidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		rv.setRemoteAdapter(R.id.event_list, intent);
		rv.setEmptyView(R.id.event_list, R.id.empty_event_list);
		rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(context));
		rv.setOnClickFillInIntent(R.id.empty_event_list,
				createOpenCalendarAtDayIntent(context, new DateTime()));
	}

	private int transparencyToDrawableRes(int bgTrans) {
		int opacity = (bgTrans - 100) * -1;
		switch (opacity) {
			case 0:
				return R.drawable.widget_background_0;
			case 10:
				return R.drawable.widget_background_10;
			case 20:
				return R.drawable.widget_background_20;
			case 30:
				return R.drawable.widget_background_30;
			case 40:
				return R.drawable.widget_background_40;
			case 50:
				return R.drawable.widget_background_50;
			case 60:
				return R.drawable.widget_background_60;
			case 70:
				return R.drawable.widget_background_70;
			case 80:
				return R.drawable.widget_background_80;
			case 90:
				return R.drawable.widget_background_90;
			case 100:
				return R.drawable.widget_background_100;
		}
		return R.drawable.widget_background_50;
	}

	public static void updateEventList(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName compName = new ComponentName(context, EventAppWidgetProvider.class);
		int[] widgetIds = appWidgetManager.getAppWidgetIds(compName);
		appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.event_list);
	}

	public static void updateAllWidgets(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName compName = new ComponentName(context, EventAppWidgetProvider.class);
		Intent intent = new Intent(context, EventAppWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetManager.getAppWidgetIds(compName));
		context.sendBroadcast(intent);
	}

	public static void updateWidget(Context context, int appWidgetId) {
		Intent intent = new Intent(context, EventAppWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		context.sendBroadcast(intent);
	}

}
