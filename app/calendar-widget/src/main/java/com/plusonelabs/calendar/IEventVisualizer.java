package com.plusonelabs.calendar;

import android.widget.RemoteViews;

import com.plusonelabs.calendar.model.Event;

import java.util.List;

public interface IEventVisualizer<T extends Event> {

	RemoteViews getRemoteView(Event eventEntry);

	int getViewTypeCount();

	List<T> getEventEntries();

	Class<? extends T> getSupportedEventEntryType();

}
