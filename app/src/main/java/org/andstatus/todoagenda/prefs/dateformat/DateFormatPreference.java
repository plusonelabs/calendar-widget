package org.andstatus.todoagenda.prefs.dateformat;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import org.andstatus.todoagenda.prefs.ApplicationPreferences;

/**
 * @author yvolk@yurivolkov.com
 */
public class DateFormatPreference extends DialogPreference {
    DateFormatValue defaultValue = DateFormatType.unknownValue();
    DateFormatValue value = DateFormatType.unknownValue();

    public DateFormatPreference(Context context) {
        super(context);
    }

    public DateFormatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        if (a.peekValue(index) != null && a.peekValue(index).type == TypedValue.TYPE_STRING) {
            return DateFormatValue.load(a.getString(index), DateFormatType.unknownValue());
        }
        return DateFormatType.unknownValue();
    }

    @Override
    public void setDefaultValue(Object defaultValue) {
        super.setDefaultValue(defaultValue);
        this.defaultValue = DateFormatValue.loadOrUnknown(defaultValue);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        value = ApplicationPreferences.getDateFormat(getContext(), getKey(), this.defaultValue);
        showValue();
    }

    public DateFormatValue getValue() {
        return value.type == DateFormatType.UNKNOWN ? defaultValue : value;
    }

    @Override
    public CharSequence getSummary() {
        return value.getSummary(getContext());
    }

    public void setValue(DateFormatValue value) {
        this.value = value;
        ApplicationPreferences.setEntryDateFormat(getContext(), value);
        showValue();
    }

    private void showValue() {
        setSummary(getSummary());
    }
}
