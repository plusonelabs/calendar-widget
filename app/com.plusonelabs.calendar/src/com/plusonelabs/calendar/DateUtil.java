package com.plusonelabs.calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import com.plusonelabs.calendar.model.EventEntry;

public class DateUtil {

	private static final String TWELVE = "12";
	private static final String NOON_AM = "12:00 AM";
	private static final String EMPTY_STRING = "";

	public static long toMidnight(long date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	public static boolean isMidnight(long date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		return calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0
				&& calendar.get(Calendar.SECOND) == 0 && calendar.get(Calendar.MILLISECOND) == 0;
	}

	public static boolean hasAmPmClock(Locale locale) {
		DateFormat stdFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
		DateFormat localeFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
		String midnight = EMPTY_STRING;
		try {
			midnight = localeFormat.format(stdFormat.parse(NOON_AM));
		} catch (ParseException ignore) {
			// we ignore this exception deliberately
		}
		return midnight.contains(TWELVE);
	}

	public static long getStartDateInUTC(EventEntry eventEntry) {
		int eventOffset = TimeZone.getDefault().getOffset(eventEntry.getStartDate());
		return eventEntry.getStartDate() - eventOffset;
	}

	public static boolean isSameDay(long date, long otherDate) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(date);
		cal2.setTimeInMillis(otherDate);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}
}
