package com.plusonelabs.calendar;

import static com.plusonelabs.calendar.EventAppWidgetProvider.*;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class EventWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new EventRemoteViewsFactory(getThemedContext(this.getApplicationContext()));
	}
}