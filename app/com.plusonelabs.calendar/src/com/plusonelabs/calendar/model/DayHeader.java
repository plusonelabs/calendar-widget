package com.plusonelabs.calendar.model;

import org.joda.time.DateTime;

public class DayHeader extends Event {
	private boolean emptyToday = false;

	public DayHeader(DateTime date) {
		setStartDate(date);
	}

	@Override
	public String toString() {
		return "DayHeader [getStartDate()=" + getStartDate() + "]";
	}

	public void setEmptyToday(boolean value) {
		emptyToday = value;
	}

	public boolean isEmptyToday()
	{
		return emptyToday;
	}

}
