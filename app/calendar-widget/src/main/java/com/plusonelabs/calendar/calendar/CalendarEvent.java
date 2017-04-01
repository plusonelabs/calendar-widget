package com.plusonelabs.calendar.calendar;

import android.content.Context;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.prefs.InstanceSettings;

import org.joda.time.DateTime;

public class CalendarEvent {
    private final Context context;
	private final int widgetId;

	private int eventId;
	private String title;
	private DateTime endDate;
	private int color;
    private boolean mHasDefaultCalendarColor;
	private boolean allDay;
	private String location;
	private boolean alarmActive;
	private boolean recurring;

    private DateTime startDate;

	public CalendarEvent(Context context, int widgetId) {
        this.context = context;
		this.widgetId = widgetId;
	}

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

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

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

    public boolean hasDefaultCalendarColor() {
        return mHasDefaultCalendarColor;
    }

    public void setDefaultCalendarColor() {
        mHasDefaultCalendarColor = true;
    }

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
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

	@Override
	public String toString() {
		return "CalendarEvent [eventId=" + eventId
				+ (title != null ? ", title=" + title : "")
				+ ", startDate=" + getStartDate()
				+ (endDate != null ? ", endDate=" + endDate : "")
				+ ", color=" + color
                + (mHasDefaultCalendarColor ? " is default" : "")
				+ ", allDay=" + allDay
				+ ", alarmActive=" + alarmActive
				+ ", recurring=" + recurring
				+ (location != null ? ", location=" + location : "") + "]";
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarEvent that = (CalendarEvent) o;
        if (eventId != that.eventId || !getStartDate().equals(that.getStartDate())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = eventId;
        result += 31 * startDate.hashCode();
        return result;
    }

    public boolean isActive() {
        return getStartDate().isBefore(DateUtil.now()) && endDate.isAfter(DateUtil.now());
    }

	public boolean isPartOfMultiDayEvent() {
		return getEndDate().withTimeAtStartOfDay().isAfter(getStartDate().withTimeAtStartOfDay());
	}

	public InstanceSettings getSettings() {
        return InstanceSettings.fromId(context, widgetId);
    }
}
