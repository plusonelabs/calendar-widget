package com.plusonelabs.calendar.widget;

import com.plusonelabs.calendar.calendar.CalendarEvent;

import org.joda.time.DateTime;

public class CalendarEntry extends WidgetEntry {

	private DateTime endDate;
	private boolean allDay;
	private boolean spansMultipleDays = false;
	private CalendarEvent event;

    public static CalendarEntry fromEvent(CalendarEvent event) {
        CalendarEntry entry = new CalendarEntry();
        entry.setStartDate(event.getStartDate());
        entry.endDate = event.getEndDate();
        entry.allDay = event.isAllDay();
        entry.event = event;
        return entry;
    }

	public String getTitle() {
		return event.getTitle();
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	public int getColor() {
		return event.getColor();
	}

	public boolean isAllDay() {
		return allDay;
 	}

	public String getLocation() {
		return event.getLocation();
	}

	public boolean isAlarmActive() {
		return event.isAlarmActive();
	}

	public boolean isRecurring() {
		return event.isRecurring();
	}

	public boolean isUndecided() { return event.isUndecided(); }

	public boolean isPartOfMultiDayEvent() {
		return spansMultipleDays;
	}

	public void setSpansMultipleDays() {
		this.spansMultipleDays = true;
	}

	public boolean isStartOfMultiDayEvent() {
		return isPartOfMultiDayEvent() && getEvent().getStartDate().equals(getStartDate());
	}

	public boolean isEndOfMultiDayEvent() {
		return isPartOfMultiDayEvent() && getEvent().getEndDate().equals(getEndDate());
	}

	public boolean spansOneFullDay() {
		return getStartDate().plusDays(1).isEqual(endDate);
	}

	public CalendarEvent getEvent() {
		return event;
	}

	@Override
	public String toString() {
		return "CalendarEntry ["
				+ "startDate=" + getStartDate()
				+ (endDate != null ? ", endDate=" + endDate : "")
				+ ", allDay=" + allDay
				+ ", spansMultipleDays=" + spansMultipleDays
				+ ", event=" + event
                + "]";
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarEntry that = (CalendarEntry) o;
        if (!event.equals(that.event) || !getStartDate().equals(that.getStartDate())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += 31 * event.hashCode();
        return result;
    }
}
