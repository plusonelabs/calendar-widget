package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.util.LazyVal;

import java.util.ArrayList;
import java.util.List;

/** See https://github.com/andstatus/todoagenda/issues/7
 * */
public enum DateFormatType {
    HIDDEN("hidden", R.string.hidden),
    DEFAULT_DEVICE("device", R.string.device_default),
    DEFAULT_ABBREVIATED("abbrev", R.string.appearance_abbreviate_dates_title),
    NUMBER_OF_DAYS("days", R.string.date_format_number_of_days_to_event),
    CUSTOM("custom-01", R.string.custom),
    UNKNOWN("unknown", R.string.not_found);

    public final String code;
    @StringRes
    public final int titleResourceId;

    public final static DateFormatType DEFAULT = DEFAULT_DEVICE;

    private final LazyVal<DateFormatValue> defaultValue = LazyVal.of( () ->
            new DateFormatValue(DateFormatType.this, ""));

    DateFormatType(String code, int titleResourceId) {
        this.code = code;
        this.titleResourceId = titleResourceId;
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
        for (DateFormatType type: values()) {
            if (type == UNKNOWN) break;
            list.add(context.getText(type.titleResourceId));
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
}
