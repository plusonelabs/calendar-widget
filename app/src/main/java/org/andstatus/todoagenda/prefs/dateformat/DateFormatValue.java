package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.util.StringUtil;

import static org.andstatus.todoagenda.prefs.dateformat.DateFormatType.CUSTOM;

/**
 * @author yvolk@yurivolkov.com
 */
public class DateFormatValue {
    public final DateFormatType type;
    public final String value;

    public DateFormatValue(DateFormatType type, String value) {
        this.type = type;
        this.value = value;
    }

    @NonNull
    public static DateFormatValue loadOrUnknown(Object defaultValue) {
        return defaultValue == null
                ? DateFormatType.unknownValue()
                : DateFormatValue.load(defaultValue.toString(), DateFormatType.unknownValue());
    }

    @NonNull
    public static DateFormatValue load(String storedValue, @NonNull DateFormatValue defaultValue) {
        return DateFormatType.load(storedValue, defaultValue);
    }

    public static DateFormatValue of(DateFormatType type, String value) {
         return type == CUSTOM && StringUtil.nonEmpty(value)
            ? new DateFormatValue(type, value)
            : type.defaultValue();
    }

    @NonNull
    public String save() {
        return type == DateFormatType.UNKNOWN ? "" : type.code + ":" + value;
    }

    public boolean hasPattern() {
        return StringUtil.nonEmpty(getPattern());
    }

    public String getPattern() {
        return StringUtil.isEmpty(value) ? type.pattern : value;
    }

    public CharSequence getSummary(Context context) {
        return context.getText(type.titleResourceId) + (type == CUSTOM ? ": \"" + value + "\"" : "");
    }
}
