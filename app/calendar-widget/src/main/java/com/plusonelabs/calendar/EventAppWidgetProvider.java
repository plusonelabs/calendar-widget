package com.plusonelabs.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.plusonelabs.calendar.prefs.InstanceSettings;
import com.plusonelabs.calendar.util.PermissionsUtil;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Locale;

import static android.graphics.Color.alpha;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static com.plusonelabs.calendar.CalendarIntentUtil.createOpenCalendarPendingIntent;
import static com.plusonelabs.calendar.RemoteViewsUtil.setAlpha;
import static com.plusonelabs.calendar.RemoteViewsUtil.setColorFilter;
import static com.plusonelabs.calendar.RemoteViewsUtil.setImageFromAttr;
import static com.plusonelabs.calendar.RemoteViewsUtil.setTextColorFromAttr;
import static com.plusonelabs.calendar.Theme.themeNameToResId;

public class EventAppWidgetProvider extends AppWidgetProvider {
    private static final String PACKAGE = EventAppWidgetProvider.class.getPackage().getName();
    public static final String ACTION_REFRESH = PACKAGE + ".action.REFRESH";

    public static int[] getWidgetIds(Context context) {
        return AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, EventAppWidgetProvider.class));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            InstanceSettings.delete(context, widgetId);
        }
    }

    @Override
	public void onUpdate(Context baseContext, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            InstanceSettings settings = InstanceSettings.fromId(baseContext, widgetId);
            AlarmReceiver.scheduleAlarm(settings.getHeaderThemeContext());
            RemoteViews rv = new RemoteViews(baseContext.getPackageName(), R.layout.widget);
            configureBackground(settings, rv);
            configureActionBar(settings, rv);
            configureList(settings, widgetId, rv);
            appWidgetManager.updateAppWidget(widgetId, rv);
        }
    }

	private void configureBackground(InstanceSettings settings, RemoteViews rv) {
		if (settings.getShowWidgetHeader()) {
			rv.setViewVisibility(R.id.action_bar, View.VISIBLE);
		} else {
			rv.setViewVisibility(R.id.action_bar, View.GONE);
		}
        int color = settings.getBackgroundColor();
        int opaqueColor = Color.rgb(red(color), green(color), blue(color));
        setColorFilter(rv, R.id.background_image, opaqueColor);
        setAlpha(rv, R.id.background_image, alpha(color));
    }

    private void configureActionBar(InstanceSettings settings, RemoteViews rv) {
        configureCurrentDate(settings, rv);
        setActionIcons(settings, rv);
        configureAddEvent(settings.getContext(), rv);
        configureRefresh(settings.getContext(), rv);
        configureOverflowMenu(settings, rv);
	}

    private void configureCurrentDate(InstanceSettings settings, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.calendar_current_date, createOpenCalendarPendingIntent(settings));
        String formattedDate = DateUtil.createDateString(settings,
                DateUtil.now(settings.getTimeZone())).toUpperCase(Locale.getDefault());
        rv.setTextViewText(R.id.calendar_current_date, formattedDate);
        setTextColorFromAttr(settings.getHeaderThemeContext(), rv, R.id.calendar_current_date, R.attr.header);
    }

    private void setActionIcons(InstanceSettings settings, RemoteViews rv) {
        setImageFromAttr(settings.getHeaderThemeContext(), rv, R.id.add_event, R.attr.header_action_add_event);
        setImageFromAttr(settings.getHeaderThemeContext(), rv, R.id.refresh, R.attr.header_action_refresh);
        setImageFromAttr(settings.getHeaderThemeContext(), rv, R.id.overflow_menu, R.attr.header_action_overflow);
        int themeId = themeNameToResId(settings.getHeaderTheme());
        int alpha = 255;
        if (themeId == R.style.Theme_Calendar_Dark || themeId == R.style.Theme_Calendar_Light) {
            alpha = 154;
        }
        setAlpha(rv, R.id.add_event, alpha);
        setAlpha(rv, R.id.refresh, alpha);
        setAlpha(rv, R.id.overflow_menu, alpha);
    }

    private void configureAddEvent(Context context, RemoteViews rv) {
        Intent intent = PermissionsUtil.getPermittedIntent(context, CalendarIntentUtil.createNewEventIntent());
        if (isIntentAvailable(context, intent)) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.add_event, pendingIntent);
        } else {
            rv.setViewVisibility(R.id.add_event, View.GONE);
        }
    }

    private void configureRefresh(Context context, RemoteViews rv) {
        Intent intent = new Intent(ACTION_REFRESH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.refresh, pendingIntent);
    }

    private void configureOverflowMenu(InstanceSettings settings, RemoteViews rv) {
        Intent intent = MainActivity.intentToConfigure(settings.getContext(), settings.getWidgetId());
        PendingIntent menuPendingIntent = PermissionsUtil.getPermittedPendingIntent(settings, intent);
        rv.setOnClickPendingIntent(R.id.overflow_menu, menuPendingIntent);
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

	private void configureList(InstanceSettings settings, int widgetId, RemoteViews rv) {
		Intent intent = new Intent(settings.getContext(), EventWidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		rv.setRemoteAdapter(R.id.event_list, intent);
		rv.setEmptyView(R.id.event_list, R.id.empty_event_list);
		rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(settings));
        rv.setOnClickFillInIntent(R.id.empty_event_list, createOpenCalendarAtDayIntent(new DateTime()));
        setTextColorFromAttr(settings.getEntryThemeContext(), rv, R.id.empty_event_list, R.attr.eventEntryTitle);
    }

    public static void updateEventList(Context context) {
        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(getWidgetIds(context), R.id.event_list);
	}

	public static void updateAllWidgets(Context context) {
		Intent intent = new Intent(context, EventAppWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getWidgetIds(context));
		context.sendBroadcast(intent);
	}

}
