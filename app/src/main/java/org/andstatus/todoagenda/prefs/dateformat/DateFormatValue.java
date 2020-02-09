package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;

import androidx.annotation.NonNull;

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

    @NonNull
    public String save() {
        return type == DateFormatType.UNKNOWN ? "" : type.code + ":" + value;
    }

    public CharSequence getSummary(Context context) {
        return context.getText(type.titleResourceId);
    }
}
