package com.plusonelabs.calendar;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.widget.RemoteViewsService;

import static com.plusonelabs.calendar.Theme.getCurrentThemeId;


public class EventWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Context appContext = getApplicationContext();
        int currentThemeId = getCurrentThemeId(appContext);
        ContextThemeWrapper context = new ContextThemeWrapper(appContext, currentThemeId);
        return new EventRemoteViewsFactory(context);
    }
}