package com.plusonelabs.calendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViewsService;

import com.plusonelabs.calendar.prefs.UniquePreferencesFragment;

import static com.plusonelabs.calendar.Theme.getCurrentThemeId;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ENTRY_THEME_DEFAULT;


public class EventWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Context appContext = getApplicationContext();
        Bundle extras = intent.getExtras();
        int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        SharedPreferences prefs = UniquePreferencesFragment.getPreferences(appContext, widgetId);
        int currentThemeId = getCurrentThemeId(appContext, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT, prefs);
        ContextThemeWrapper context = new ContextThemeWrapper(appContext, currentThemeId);
        return new EventRemoteViewsFactory(context, prefs);
    }
}