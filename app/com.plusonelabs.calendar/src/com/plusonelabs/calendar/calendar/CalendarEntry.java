package com.plusonelabs.calendar.calendar;

import android.text.format.DateUtils;
import android.util.FloatMath;

import com.plusonelabs.calendar.model.EventEntry;

public class CalendarEntry extends EventEntry {

	private int eventId;
	private String title;
	private long endDate;
	private int color;
	private boolean allDay;
	private boolean alarmActive;
	private boolean recurring;
	private boolean spansMultipleDays;

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public void setAlarmActive(boolean active) {
		this.alarmActive = active;
	}

	public boolean isAlarmActive() {
		return alarmActive;
	}

	public void setRecurring(boolean recurring) {
		this.recurring = recurring;
	}

	public boolean isRecurring() {
		return recurring;
	}

	public boolean isPartOfMultiDayEvent() {
		return spansMultipleDays;
	}

	public void setSpansMultipleDays(boolean spansMultipleDays) {
		this.spansMultipleDays = spansMultipleDays;
	}

	public int daysCovered() {
		return (int) FloatMath.ceil((endDate - getStartDate()) / (1000f * 60f * 60f * 24f));
	}

	public boolean spansFullDay() {
		return getStartDate() + DateUtils.DAY_IN_MILLIS == endDate;
	}

	public int compareTo(CalendarEntry otherEntry) {
		if (isSameDay(otherEntry.getStartDate())) {
			if (allDay) {
				return -1;
			} else if (otherEntry.allDay) {
				return 1;
			}
		}
		return super.compareTo(otherEntry);
	}

	@Override
	protected CalendarEntry clone() {
		CalendarEntry clone = new CalendarEntry();
		clone.setStartDate(getStartDate());
		clone.endDate = endDate;
		clone.eventId = eventId;
		clone.title = title;
		clone.allDay = allDay;
		clone.color = color;
		clone.alarmActive = alarmActive;
		clone.recurring = recurring;
		clone.spansMultipleDays = spansMultipleDays;
		return clone;
	}

	@Override
	public String toString() {
		return "CalendarEntry [eventId=" + eventId + ", title=" + title + ", endDate=" + endDate
				+ ", color=" + color + ", allDay=" + allDay + ", alarmActive=" + alarmActive
				+ ", recurring=" + recurring + ", spansMultipleDays=" + spansMultipleDays
				+ ", getStartDate()=" + getStartDate() + "]";
	}

}
