package com.plusonelabs.calendar;

import java.util.Calendar;

public class DateUtil {

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
}
