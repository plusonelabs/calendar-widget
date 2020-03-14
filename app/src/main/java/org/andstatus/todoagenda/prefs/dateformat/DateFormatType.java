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

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.util.LazyVal;
import org.andstatus.todoagenda.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/** See https://github.com/andstatus/todoagenda/issues/7 */
public enum DateFormatType {
    HIDDEN("hidden", R.string.hidden, ""),
    DEVICE_DEFAULT("deviceDefault", R.string.device_default, ""),
    DEFAULT_WEEKDAY("defaultWeekday", R.string.date_format_default_weekday, ""),
    DEFAULT_YTT("defaultYtt", R.string.date_format_default_ytt, ""),
    DEFAULT_DAYS("defaultDays", R.string.date_format_default_days, ""),
    ABBREVIATED("abbrev", R.string.appearance_abbreviate_dates_title, ""),
    NUMBER_OF_DAYS("days", R.string.date_format_number_of_days_to_event, "bbbb"),
    DAY_IN_MONTH("dayInMonth", R.string.date_format_day_in_month, "dd"),
    MONTH_DAY("monthDay", R.string.date_format_month_day, "MM-dd"),
    WEEK_IN_YEAR("weekInYear", R.string.date_format_week_in_year, "ww"),
    DEFAULT_EXAMPLE("example", R.string.pattern_example, "BBB, EEEE d MMM yyyy, BBBB"),
    PATTERN_EXAMPLE1("example1", R.string.pattern_example, "b 'days,' EEE, d MMM yyyy, 'week' ww"),
    CUSTOM("custom-01", R.string.custom_pattern, ""),
    UNKNOWN("unknown", R.string.not_found, "");

    public final String code;
    @StringRes
    public final int titleResourceId;
    public final String pattern;

    public final static DateFormatType DEFAULT = DEFAULT_WEEKDAY;

    private final LazyVal<DateFormatValue> defaultValue = LazyVal.of( () ->
            new DateFormatValue(DateFormatType.this, ""));

    DateFormatType(String code, int titleResourceId, String pattern) {
        this.code = code;
        this.titleResourceId = titleResourceId;
        this.pattern = pattern;
    }

    @NonNull
    public static DateFormatValue load(String storedValue, @NonNull DateFormatValue defaultValue) {
        DateFormatType formatType = DateFormatType.load(storedValue, UNKNOWN);
        switch (formatType) {
            case UNKNOWN:
                return defaultValue;
            case CUSTOM:
                return new DateFormatValue(formatType, storedValue.substring(CUSTOM.code.length() + 1));
            default:
                return formatType.defaultValue();
        }
    }

    @NonNull
    private static DateFormatType load(String storedValue, @NonNull DateFormatType defaultType) {
        if (storedValue == null) return defaultType;

        for (DateFormatType type: values()) {
            if (storedValue.startsWith( type.code + ":")) return type;
        }
        return defaultType;
    }

    public static DateFormatValue unknownValue() {
        return UNKNOWN.defaultValue();
    }

    public static List<CharSequence> getSpinnerEntryList(Context context) {
        List<CharSequence> list = new ArrayList<>();
        int exampleInd = 0;
        for (DateFormatType type: values()) {
            if (type == UNKNOWN) break;
            list.add(context.getText(type.titleResourceId) + (type.isPatternExample() ? " " + (++exampleInd) : ""));
        }
        return list;
    }

    public DateFormatValue defaultValue() {
        return defaultValue.get();
    }

    public int getSpinnerPosition() {
        for (int position = 0; position < values().length; position++) {
            DateFormatType type = values()[position];
            if (type == UNKNOWN) break;
            if (type == this) return position;
        }
        return 0;
    }

    public boolean hasPattern() {
        return StringUtil.nonEmpty(pattern);
    }

    public boolean isPatternExample() {
        return titleResourceId == R.string.pattern_example;
    }

    public boolean isCustomPattern() {
        return isPatternExample() || this == CUSTOM;
    }

    public DateFormatType toSave() {
        return isCustomPattern() ? CUSTOM : this;
    }
}
