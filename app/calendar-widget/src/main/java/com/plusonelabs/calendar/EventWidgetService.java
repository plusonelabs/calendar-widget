package com.plusonelabs.calendar;

import android.content.Intent;
import android.widget.RemoteViewsService;

import static com.plusonelabs.calendar.EventAppWidgetProvider.getThemedContext;

public class EventWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new EventRemoteViewsFactory(getThemedContext(this.getApplicationContext()));
	}
}