/*
 * Copyright (C) 2018 yvolk (Yuri Volkov), http://yurivolkov.com
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

package org.andstatus.todoagenda.util;

import androidx.annotation.NonNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Adds convenience methods to {@link StringBuilder} */
public class MyStringBuilder implements CharSequence, IsEmpty {
    public static final String COMMA = ",";
    public final StringBuilder builder;

    public static MyStringBuilder of(CharSequence text) {
        return new MyStringBuilder(text);
    }

    private MyStringBuilder(CharSequence text) {
        this(new StringBuilder(text));
    }

    public MyStringBuilder() {
        this(new StringBuilder());
    }

    public MyStringBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    public static MyStringBuilder of(Optional<String> content) {
        return content.map(MyStringBuilder::of).orElse(new MyStringBuilder());
    }

    @NonNull
    public static String formatKeyValue(Object keyIn, Object valueIn) {
        String key = objToTag(keyIn);
        if (keyIn == null) {
            return key;
        }
        String value = "null";
        if (valueIn != null) {
            value = valueIn.toString();
        }
        return formatKeyValue(key, value);
    }

    /** Strips value from leading and trailing commas */
    @NonNull
    public static String formatKeyValue(Object key, String value) {
        String out = "";
        if (!StringUtil.isEmpty(value)) {
            out = value.trim();
            if (out.substring(0, 1).equals(COMMA)) {
                out = out.substring(1);
            }
            int ind = out.lastIndexOf(COMMA);
            if (ind > 0 && ind == out.length()-1) {
                out = out.substring(0, ind);
            }
        }
        return objToTag(key) + ":{" + out + "}";
    }

    @NonNull
    public static String objToTag(Object objTag) {
        final String tag;
        if (objTag == null) {
            tag = "(null)";
        } else if (objTag instanceof String) {
            tag = (String) objTag;
        } else if (objTag instanceof Enum<?>) {
            tag = objTag.toString();
        } else if (objTag instanceof Class<?>) {
            tag = ((Class<?>) objTag).getSimpleName();
        }else {
            tag = objTag.getClass().getSimpleName();
        }
        if (tag.trim().isEmpty()) {
            return "(empty)";
        }
        return tag;
    }

    @NonNull
    public <T> MyStringBuilder withCommaNonEmpty(CharSequence label, T obj) {
        return withComma(label, obj, MyStringBuilder::nonEmptyObj);
    }

    @NonNull
    public static <T> boolean nonEmptyObj(T obj) {
        return !isEmptyObj(obj);
    }

    @NonNull
    public static <T> boolean isEmptyObj(T obj) {
        if (obj instanceof IsEmpty) return ((IsEmpty) obj).isEmpty();
        if (obj instanceof Number) return ((Number) obj).longValue() == 0;
        if (obj instanceof String) return StringUtil.isEmpty((String) obj);
        return obj == null;
    }

    @NonNull
    public <T> MyStringBuilder withComma(CharSequence label, T obj, Predicate<T> predicate) {
        return obj == null || !predicate.test(obj)
                ? this
                : withComma(label, obj);
    }

    @NonNull
    public MyStringBuilder withComma(CharSequence label, Object obj, Supplier<Boolean> filter) {
        return obj == null || !filter.get()
                ? this
                : withComma(label, obj);
    }

    @NonNull
    public MyStringBuilder withComma(CharSequence label, Object obj) {
        return append(label, obj, ", ", false);
    }

    @NonNull
    public MyStringBuilder withCommaQuoted(CharSequence label, Object obj, boolean quoted) {
        return append(label, obj, ", ", quoted);
    }

    @NonNull
    public MyStringBuilder withComma(CharSequence text) {
        return withSeparator(text, ", ");
    }

    @NonNull
    public MyStringBuilder withSpaceQuoted(CharSequence text) {
        return append("", text, " ", true);
    }

    @NonNull
    public MyStringBuilder withSpace(CharSequence text) {
        return withSeparator(text, " ");
    }

    @NonNull
    public MyStringBuilder withSeparator(CharSequence text, String separator) {
        return append("", text, separator, false);
    }

    public MyStringBuilder atNewLine(CharSequence label, CharSequence text) {
        return append(label, text, ", \n", false);
    }

    public MyStringBuilder atNewLine(CharSequence text) {
        return append("", text, ", \n", false);
    }

    @NonNull
    public MyStringBuilder append(CharSequence label, Object obj, @NonNull String separator, boolean quoted) {
        if (obj == null) return this;

        String text = obj.toString();
        if (StringUtil.isEmpty(text)) return this;

        if (builder.length() > 0) builder.append(separator);
        if (StringUtil.nonEmpty(label)) builder.append(label).append(": ");
        if (quoted) builder.append("\"");
        builder.append(text);
        if (quoted) builder.append("\"");
        return this;
    }

    @NonNull
    public MyStringBuilder append(CharSequence text) {
        if (StringUtil.nonEmpty(text)) {
            builder.append(text);
        }
        return this;
    }

    @Override
    public int length() {
        return builder.length();
    }

    @Override
    public char charAt(int index) {
        return builder.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return builder.subSequence(start, end);
    }

    @Override
    @NonNull
    public String toString() {
        return builder.toString();
    }

    @NonNull
    public static StringBuilder appendWithSpace(StringBuilder builder, CharSequence text) {
        return new MyStringBuilder(builder).withSpace(text).builder;
    }

    @NonNull
    public MyStringBuilder prependWithSeparator(CharSequence text, @NonNull String separator) {
        if (text.length() > 0) {
            builder.insert(0, separator);
            builder.insert(0, text);
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return length() == 0;
    }
}