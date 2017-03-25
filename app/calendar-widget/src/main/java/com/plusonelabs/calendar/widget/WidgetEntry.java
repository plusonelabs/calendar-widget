package com.plusonelabs.calendar.widget;

import android.content.Context;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.R;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class WidgetEntry implements Comparable<WidgetEntry> {

	private DateTime startDate;

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

    public DateTime getStartDay() {
        return getStartDate().withTimeAtStartOfDay();
    }

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [startDate=" + startDate + "]";
	}

	public CharSequence getDateString(Context context) {
        Days days = Days.daysBetween(DateUtil.now().withTimeAtStartOfDay(), startDate.withTimeAtStartOfDay());
        switch (days.getDays()) {
            case -1:
                return context.getText(R.string.yesterday);
            case 0:
                return context.getText(R.string.today);
            case 1:
                return context.getText(R.string.tomorrow);
            default:
                return Integer.toString(days.getDays());
        }
	}

    @Override
	public int compareTo(WidgetEntry otherEvent) {
		if (getStartDate().isAfter(otherEvent.getStartDate())) {
			return 1;
		} else if (getStartDate().isBefore(otherEvent.getStartDate())) {
			return -1;
		}
		return 0;
	}

}
