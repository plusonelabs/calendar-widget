package org.andstatus.todoagenda.widget;

import android.widget.RemoteViews;

import java.util.List;

public abstract class WidgetEntryVisualizer<T extends WidgetEntry<T>> {

    public abstract RemoteViews getRemoteView(WidgetEntry eventEntry);

    public abstract int getViewTypeCount();

    public abstract List<T> getEventEntries();

}