package com.plusonelabs.calendar;

import static com.plusonelabs.calendar.CalendarIntentUtil.*;
import static com.plusonelabs.calendar.RemoteViewsUtil.*;
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
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.RemoteViews;

public class EventAppWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context baseContext, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Context context = getThemedContext(baseContext);
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

	public static ContextThemeWrapper getThemedContext(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Theme theme = Theme.valueOf(prefs.getString(PREF_THEME, PREF_THEME_DEFAULT));
		int themeId = R.style.Theme_Calendar;
		if (theme == Theme.LIGHT) {
			themeId = R.style.Theme_Calendar_Light;
		}
		return new ContextThemeWrapper(context, themeId);
	}

	public void configureBackground(Context context, RemoteViews rv) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean(PREF_SHOW_HEADER, true)) {
			rv.setViewVisibility(R.id.action_bar, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.action_bar, View.GONE);
		}
		int bgTrans = prefs.getInt(PREF_BACKGROUND_TRANSPARENCY,
				PREF_BACKGROUND_TRANSPARENCY_DEFAULT);
		setAlpha(rv, R.id.background_image, Math.round(bgTrans / 100f * 255f));
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
		setTextColorRes(context, rv, R.id.empty_event_list, R.attr.eventEntryTitle);
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
