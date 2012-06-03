package com.plusonelabs.calendar.model;

public class DayHeader extends CalenderEntry {

	public DayHeader(long date) {
		setStartDate(date);
	}

	@Override
	public String toString() {
		return "DayHeader [getStartDate()=" + getStartDate() + "]";
	}

}
