package com.plusonelabs.calendar.widget;

import com.plusonelabs.calendar.DateUtil;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class WidgetEntry implements Comparable<WidgetEntry> {

    private DateTime startDate;

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getStartDay() {
        return getStartDate().withTimeAtStartOfDay();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [startDate=" + startDate + "]";
    }

    public int getDaysFromToday() {
        return Days.daysBetween(DateUtil.now(startDate.getZone()).withTimeAtStartOfDay(),
                startDate.withTimeAtStartOfDay()).getDays();
    }

    @Override
    public int compareTo(WidgetEntry otherEvent) {
        if (getStartDate().isAfter(otherEvent.getStartDate())) {
            return 1;
        } else if (getStartDate().isBefore(otherEvent.getStartDate())) {
            return -1;
        }
        return 0;
    }
}
