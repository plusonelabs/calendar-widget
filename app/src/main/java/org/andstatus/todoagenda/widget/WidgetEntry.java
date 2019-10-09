package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;

public abstract class WidgetEntry<T extends WidgetEntry<T>> implements Comparable<WidgetEntry<T>> {

    private DateTime startDate;
    private DateTime endDate;

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
        endDate = DateUtil.startOfNextDay(startDate);
    }

    public DateTime getStartDay() {
        return getStartDate().withTimeAtStartOfDay();
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public OrderedEventSource getSource() {
        return OrderedEventSource.EMPTY;
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
        return Integer.signum(getSource().order - otherEvent.getSource().order);
    }

    public TimeSection getStartDaySection() {
        return DateUtil.isBeforeToday(getStartDate())
                ? TimeSection.PAST
                : (DateUtil.isToday(getStartDate()) ? TimeSection.TODAY : TimeSection.FUTURE);
    }

    public TimeSection getEndTimeSection() {
        return DateUtil.isBeforeNow(getEndDate())
                ? TimeSection.PAST
                : (DateUtil.isToday(getStartDate()) ? TimeSection.TODAY : TimeSection.FUTURE);
    }
}
