package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.provider.EventProviderType;

public class EventSource {

    public final EventProviderType providerType;
    private int id;
    private String title;
    private String summary;
    private int color;

    public EventSource(EventProviderType providerType, int id, String title, String summary, int color) {
        this.providerType = providerType;
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public int getColor() {
        return color;
    }
}
