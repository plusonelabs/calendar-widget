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

	public boolean isSameDay(DateTime otherDate) {
		DateTime from = otherDate.withTimeAtStartOfDay();
		DateTime to = otherDate.plusDays(1).withTimeAtStartOfDay();
		if((startDate.isAfter(from) || startDate.isEqual(from)) && startDate.isBefore(to)) {
			return true;
		}
		return false;
	}

	public boolean isToday() {
		return isSameDay(new DateTime());
	}

	public boolean isTomorrow() {
		return isSameDay(new DateTime().plusDays(1));
	}

	@Override
	public String toString() {
		return "CalenderEntry [startDate=" + startDate + "]";
	}

	public int compareTo(Event otherEvent) {
		if (getStartDate().isAfter(otherEvent.getStartDate())) {
			return 1;
		} else if (getStartDate().isBefore(otherEvent.getStartDate())) {
			return -1;
		}
		return 0;
	}

}
