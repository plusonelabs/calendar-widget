package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;

public abstract class WidgetEntry<T extends WidgetEntry<T>> implements Comparable<WidgetEntry<T>> {

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

    public abstract int getPriority();

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
        return Integer.signum(getPriority() - otherEvent.getPriority());
    }

    public boolean isBeforeToday() {
        return DateUtil.isBeforeToday(getStartDate());
    }

    public boolean isToday() {
        return DateUtil.isToday(getStartDate());
    }

    public boolean isAfterToday() {
        return DateUtil.isAfterToday(getStartDate());
    }
}
