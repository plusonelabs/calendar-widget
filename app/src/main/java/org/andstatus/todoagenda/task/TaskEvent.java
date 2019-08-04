package org.andstatus.todoagenda.task;

import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TaskEvent {

    private long id;
    private String title;
    private final DateTimeZone zone;
    private DateTime startDate;
    private DateTime dueDate;
    private int color;

    public TaskEvent(DateTimeZone zone) {
        this.zone = zone;
    }

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

    public DateTime getDueDate() {
        return dueDate;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setDates(Long startMillis, Long dueMillis) {
        startDate = toStartDate(startMillis, dueMillis);
        dueDate = toDueDate(startMillis, dueMillis);
    }

    private DateTime toStartDate(Long startMillis, Long dueMillis) {
        DateTime startDate;
        if (startMillis != null) {
            startDate = new DateTime(startMillis, zone);
        } else {
            if (dueMillis != null) {
                startDate = new DateTime(dueMillis, zone);
            } else {
                startDate = DateUtil.now(zone).withTimeAtStartOfDay();
            }
        }
        return startDate;
    }

    private DateTime toDueDate(Long startMillis, Long dueMillis) {
        DateTime dueDate = dueMillis == null
                ? DateUtil.now(zone).plusDays(1).withTimeAtStartOfDay().minusSeconds(1)  // End of today
                : new DateTime(dueMillis, zone);
        return startMillis == null
                ? dueDate.plusSeconds(1)
                : dueDate;
    }
}
