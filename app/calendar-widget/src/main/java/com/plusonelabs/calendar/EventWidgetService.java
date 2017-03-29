package com.plusonelabs.calendar;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViewsService;

import org.joda.time.DateTimeZone;

import static com.plusonelabs.calendar.Theme.getCurrentThemeId;
import static com.plusonelabs.calendar.prefs.ApplicationPreferences.PREF_ENTRY_THEME;
import static com.plusonelabs.calendar.prefs.ApplicationPreferences.PREF_ENTRY_THEME_DEFAULT;


public class EventWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Context appContext = getApplicationContext();
        int currentThemeId = getCurrentThemeId(appContext, PREF_ENTRY_THEME, PREF_ENTRY_THEME_DEFAULT);
        ContextThemeWrapper context = new ContextThemeWrapper(appContext, currentThemeId);
        DateTimeZone.setDefault(DateUtil.getCurrentTimeZone(context));
        return new EventRemoteViewsFactory(context);
    }
}