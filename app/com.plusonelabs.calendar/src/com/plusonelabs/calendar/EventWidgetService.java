package com.plusonelabs.calendar;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class EventWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new EventRemoteViewsFactory(this.getApplicationContext());
	}
}