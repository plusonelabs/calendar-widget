package com.plusonelabs.calendar.model;

import java.util.Calendar;

import android.text.format.DateUtils;

public class EventEntry implements Comparable<EventEntry> {

	private long startDate;

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public boolean isToday() {
		return isSameDay(System.currentTimeMillis());
	}

	public boolean isSameDay(long otherDate) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(startDate);
		cal2.setTimeInMillis(otherDate);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	public boolean isTomorrow() {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(getStartDate());
		cal2.setTimeInMillis(System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	@Override
	public String toString() {
		return "CalenderEntry [startDate=" + startDate + "]";
	}

	public int compareTo(EventEntry otherEvent) {
		if (getStartDate() > otherEvent.getStartDate()) {
			return 1;
		} else if (getStartDate() < otherEvent.getStartDate()) {
			return -1;
		}
		return 0;
	}

}
