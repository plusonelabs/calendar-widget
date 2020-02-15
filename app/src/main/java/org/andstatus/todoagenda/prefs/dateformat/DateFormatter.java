/*
 * Copyright (c) 2019 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;
import android.text.format.DateUtils;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.util.StringUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class DateFormatter {
    private static final char NUMBER_OF_DAYS_LETTER = 'b';
    private final Context context;
    private final DateFormatValue dateFormatValue;
    private final DateTime now;
    Locale locale = Locale.getDefault();

    public DateFormatter(Context context, DateFormatValue dateFormatValue, DateTime now) {
        this.context = context;
        this.dateFormatValue = dateFormatValue;
        this.now = now;
    }

    public CharSequence formatDate(DateTime date) {
        try {
            if(dateFormatValue.hasPattern()) {
                return formatDateCustom(date, dateFormatValue.getPattern());
            }

            switch (dateFormatValue.type) {
                case HIDDEN:
                    return "";
                case DEVICE_DEFAULT:
                    return formatDateTime(date, DateUtils.FORMAT_SHOW_DATE);
                case DEFAULT_WEEKDAY:
                    return formatDateTime(date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
                case ABBREVIATED:
                    return formatDateTime(date, DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_WEEKDAY);
                case DEFAULT_DAYS:
                    return getNumberOfDaysToEventString(context, 5, getNumberOfDaysToEvent(date)) + ", " +
                            formatDateTime(date, DateUtils.FORMAT_SHOW_DATE);
                case NUMBER_OF_DAYS:
                    return getNumberOfDaysToEventString(context, 5, getNumberOfDaysToEvent(date));
                default:
                    return "(not implemented)";
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    private String formatDateTime(DateTime date, int flags) {
        long millis = toJavaDate(date).getTime();
        return DateUtils.formatDateRange(context,
                new Formatter(new StringBuilder(50), locale),
                millis,
                millis,
                flags,
                date.getZone().getID())
                .toString();
    }

    public static Date toJavaDate(DateTime date) {
        return new Date(date.getYearOfEra() - 1900, date.getMonthOfYear() - 1, date.getDayOfMonth());
    }

    public static CharSequence getNumberOfDaysToEventString(Context context, int formatLength, int daysToEvent) {
        if (formatLength >= 4) {
            switch (daysToEvent) {
                case -1:
                    return context.getText(R.string.yesterday);
                case 0:
                    return context.getText(R.string.today);
                case 1:
                    return context.getText(R.string.tomorrow);
                default:
                    break;
            }
        }
        if (Math.abs(daysToEvent) > 9999) return "...";

        CharSequence days1 = Integer.toString(daysToEvent);
        if (days1.length() > formatLength) return days1;

        return String.format("%0" + formatLength + "d", daysToEvent);
    }

    public int getNumberOfDaysToEvent(DateTime date) {
        return Days.daysBetween(
                now.withZone(date.getZone()).withTimeAtStartOfDay(),
                date.withTimeAtStartOfDay())
            .getDays();
    }

    private String formatDateCustom(DateTime date, String pattern) {
        if (StringUtil.isEmpty(pattern)) return "";

        try {
            String pattern2 = preProcessNumberOfDaysToEvent(date, pattern);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern2, locale);
            return simpleDateFormat.format(toJavaDate(date));
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    private String preProcessNumberOfDaysToEvent(DateTime date, String pattern) {
        int ind1 = getIndexOfNumberOfDaysLetter(pattern);
        if (ind1 < 0) return pattern;

        int ind2 = ind1;
        while (ind2 < pattern.length() && pattern.charAt(ind2) == NUMBER_OF_DAYS_LETTER) {
            ind2++;
        }
        CharSequence result = getNumberOfDaysToEventString(context, ind2 - ind1, getNumberOfDaysToEvent(date));
        return (ind1 > 0 ? pattern.substring(0, ind1) : "") +
               "'" + result + "'" +
               (ind2 < pattern.length() ? pattern.substring(ind2) : "");
    }

    private int getIndexOfNumberOfDaysLetter(String pattern) {
        boolean inQuotes = false;
        for (int ind = 0; ind < pattern.length(); ind++) {
            if ((pattern.charAt(ind) == NUMBER_OF_DAYS_LETTER) && !inQuotes) return ind;

            if (pattern.charAt(ind) == '\'') inQuotes = !inQuotes;
        }
        return -1;
    }

}