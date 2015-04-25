package com.plusonelabs.calendar.model;

import org.joda.time.DateTime;

public class Event implements Comparable<Event> {

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

	public boolean startsSameDay(DateTime otherDate) {
		return getStartDay().isEqual(otherDate.withTimeAtStartOfDay());
	}

	public boolean startsToday() {
		return startsSameDay(new DateTime());
	}

	public boolean startsTomorrow() {
		return startsSameDay(new DateTime().plusDays(1));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [startDate=" + startDate + "]";
	}

    @Override
	public int compareTo(Event otherEvent) {
		if (getStartDate().isAfter(otherEvent.getStartDate())) {
			return 1;
		} else if (getStartDate().isBefore(otherEvent.getStartDate())) {
			return -1;
		}
		return 0;
	}

}
