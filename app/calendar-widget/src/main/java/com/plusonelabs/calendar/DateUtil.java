package com.plusonelabs.calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

import org.joda.time.DateTime;

public class DateUtil {

	private static final String TWELVE = "12";
	private static final String NOON_AM = "12:00 AM";
	private static final String EMPTY_STRING = "";

	public static boolean isMidnight(DateTime date) {
		return date.isEqual(date.toDateMidnight());
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

}
