package org.andstatus.todoagenda.prefs;

public class EventSource {

    private int id;
    private String title;
    private String summary;
    private int color;

    public EventSource(int id, String title, String summary, int color) {
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
