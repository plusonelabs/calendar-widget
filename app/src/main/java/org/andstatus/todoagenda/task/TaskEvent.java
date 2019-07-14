package org.andstatus.todoagenda.task;

import android.content.Intent;
import org.joda.time.DateTime;

public abstract class TaskEvent {
    private long id;
    private String title;
    private DateTime startDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public abstract Intent createOpenCalendarEventIntent();
}
