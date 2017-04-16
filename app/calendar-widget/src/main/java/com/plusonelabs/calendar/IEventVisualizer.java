package com.plusonelabs.calendar;

import android.widget.RemoteViews;

import com.plusonelabs.calendar.widget.WidgetEntry;

import java.util.List;

public interface IEventVisualizer<T extends WidgetEntry> {

    RemoteViews getRemoteView(WidgetEntry eventEntry);

    int getViewTypeCount();

    List<T> getEventEntries();

    Class<? extends T> getSupportedEventEntryType();

}
