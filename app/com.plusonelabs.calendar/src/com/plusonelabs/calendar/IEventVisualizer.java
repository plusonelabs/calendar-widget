package com.plusonelabs.calendar;

import java.util.ArrayList;

import android.widget.RemoteViews;

import com.plusonelabs.calendar.model.Event;

public interface IEventVisualizer<T extends Event> {

	RemoteViews getRemoteView(Event eventEntry);

	int getViewTypeCount();

	ArrayList<T> getEventEntries();

	Class<? extends T> getSupportedEventEntryType();

}
