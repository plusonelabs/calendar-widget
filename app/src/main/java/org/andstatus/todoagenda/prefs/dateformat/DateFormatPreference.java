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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import org.andstatus.todoagenda.prefs.ApplicationPreferences;

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
        ApplicationPreferences.setDateFormat(getContext(), getKey(), value);
        showValue();
    }

    private void showValue() {
        setSummary(getSummary());
    }
}