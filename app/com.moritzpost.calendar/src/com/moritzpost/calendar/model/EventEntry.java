package com.moritzpost.calendar.model;

public class EventEntry extends CalenderEntry implements Comparable<EventEntry> {

	private int eventId;
	private String title;
	private long endDate;
	private int color;
	private boolean allDay;

	public EventEntry() {

	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public EventEntry(String title, long startDate) {
		this.title = title;
		setStartDate(startDate);
	}

	public EventEntry(String title, long startDate, long endDate) {
		this(title, startDate);
		this.endDate = endDate;
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

	public int compareTo(EventEntry appo) {
		if (isSameDay(appo.getStartDate())) {
			if (allDay) {
				return -1;
			} else if (appo.allDay) {
				return 1;
			}
		}
		if (getStartDate() > appo.getStartDate()) {
			return 1;
		} else if (getStartDate() < appo.getStartDate()) {
			return -1;
		}
		return 0;
	}
}
