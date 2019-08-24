package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProvider;

import java.util.List;

import androidx.annotation.NonNull;

public abstract class WidgetEntryVisualizer<T extends WidgetEntry<T>> {
    private final EventProvider eventProvider;

    public WidgetEntryVisualizer(EventProvider eventProvider) {
        this.eventProvider = eventProvider;
    }

    public abstract RemoteViews getRemoteView(WidgetEntry eventEntry);

    @NonNull
    protected InstanceSettings getSettings() {
        return eventProvider.getSettings();
    }

    public Context getContext() {
        return eventProvider.context;
    }

    public abstract int getViewTypeCount();

    public abstract List<T> getEventEntries();

}