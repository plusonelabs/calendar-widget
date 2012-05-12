package com.moritzpost.calendar.model;

import java.util.Calendar;

public class CalenderEntry {

	private static final long ONE_DAY = 86400000;

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
		cal2.setTimeInMillis(System.currentTimeMillis() + ONE_DAY);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

}
