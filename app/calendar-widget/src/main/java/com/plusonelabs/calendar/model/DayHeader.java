package com.plusonelabs.calendar.model;

import org.joda.time.DateTime;

public class DayHeader extends Event {

	public DayHeader(DateTime date) {
		setStartDate(date.withTimeAtStartOfDay());
	}

}
