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

import org.andstatus.todoagenda.util.StringUtil;

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
         return type.isCustomPattern() && StringUtil.nonEmpty(value)
            ? new DateFormatValue(type, value)
            : type.defaultValue();
    }

    @NonNull
    public String save() {
        if (type == type.toSave()) {
            return type == DateFormatType.UNKNOWN
                    ? ""
                    : type.code + ":" + getPattern();
        } else {
            return toSave().save();
        }
    }

    public boolean hasPattern() {
        return StringUtil.nonEmpty(getPattern());
    }

    public String getPattern() {
        return StringUtil.isEmpty(value) ? type.pattern : value;
    }

    public CharSequence getSummary(Context context) {
        return context.getText(type.titleResourceId) + (type.isCustomPattern() ? ": " + value : "");
    }

    public DateFormatValue toSave() {
        return DateFormatValue.of(type.toSave(), value);
    }
}
