package org.andstatus.todoagenda;

import android.widget.RemoteViews;

import org.andstatus.todoagenda.widget.WidgetEntry;

import java.util.List;

public interface IEventVisualizer<T extends WidgetEntry> {

    RemoteViews getRemoteView(WidgetEntry eventEntry);

    int getViewTypeCount();

    List<T> getEventEntries();

    Class<? extends T> getSupportedEventEntryType();

}
