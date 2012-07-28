package com.plusonelabs.calendar;

import java.util.ArrayList;

import android.widget.RemoteViews;

import com.plusonelabs.calendar.model.EventEntry;

public interface IEventVisualizer<T extends EventEntry> {

	RemoteViews getRemoteView(EventEntry eventEntry);

	int getViewTypeCount();

	ArrayList<T> getEventEntries();

	Class<? extends T> getSupportedEventEntryType();

}
