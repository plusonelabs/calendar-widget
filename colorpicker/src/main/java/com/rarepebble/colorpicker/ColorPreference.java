/*
 * Copyright (C) 2015 Martin Stone
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

package com.rarepebble.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class ColorPreference extends DialogPreference {
	final String selectNoneButtonText;
	Integer defaultColor;
	private final String noneSelectedSummaryText;
	private final CharSequence summaryText;
	final boolean showAlpha;
	final boolean showHex;
	final boolean showPreview;
	private View thumbnail;
	private ColorPickerView mPicker = null;

	public ColorPreference(Context context) {
		this(context, null);
	}

	public ColorPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		summaryText = super.getSummary();

		if (attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorPicker, 0, 0);
			selectNoneButtonText = a.getString(R.styleable.ColorPicker_colorpicker_selectNoneButtonText);
			noneSelectedSummaryText = a.getString(R.styleable.ColorPicker_colorpicker_noneSelectedSummaryText);
			showAlpha = a.getBoolean(R.styleable.ColorPicker_colorpicker_showAlpha, true);
			showHex = a.getBoolean(R.styleable.ColorPicker_colorpicker_showHex, true);
			showPreview = a.getBoolean(R.styleable.ColorPicker_colorpicker_showPreview, true);
		}
		else {
			selectNoneButtonText = null;
			noneSelectedSummaryText = null;
			showAlpha = true;
			showHex = true;
			showPreview = true;
		}
	}

	public void onBindViewHolder(PreferenceViewHolder viewHolder) {
		thumbnail = addThumbnail(viewHolder.itemView);
		showColor(getPersistedIntDefaultOrNull());
		// Only call after showColor sets any summary text:
		super.onBindViewHolder(viewHolder);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		defaultColor = readDefaultValue(a, index);
		return defaultColor;
	}

	private static Integer readDefaultValue(TypedArray a, int index) {
		if (a.peekValue(index) != null) {
			int type = a.peekValue(index).type;
			if (type == TypedValue.TYPE_STRING) {
				return Color.parseColor(standardiseColorDigits(a.getString(index)));
			}
			else if (TypedValue.TYPE_FIRST_COLOR_INT <= type && type <= TypedValue.TYPE_LAST_COLOR_INT) {
				return a.getColor(index, Color.GRAY);
			}
			else if (TypedValue.TYPE_FIRST_INT <= type && type <= TypedValue.TYPE_LAST_INT) {
				return a.getInt(index, Color.GRAY);
			}
		}
		return null;
	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		super.setDefaultValue(defaultValue);
		defaultColor = parseDefaultValue(defaultValue);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		setColor(restorePersistedValue ? getColor() : parseDefaultValue(defaultValue));
	}

	private static int parseDefaultValue(Object defaultValue) {
		return (defaultValue == null)
				? Color.GRAY
				: (defaultValue instanceof Integer)
					? (Integer)defaultValue
					: Color.parseColor(standardiseColorDigits(defaultValue.toString()));
	}

	private static String standardiseColorDigits(String s) {
		if (s.charAt(0) == '#' && s.length() <= "#argb".length()) {
			// Convert #[a]rgb to #[aa]rrggbb
			String ss = "#";
			for (int i = 1; i < s.length(); ++i) {
				ss += s.charAt(i);
				ss += s.charAt(i);
			}
			return ss;
		}
		else {
			return s;
		}
	}

	private View addThumbnail(View view) {
		LinearLayout widgetFrameView = ((LinearLayout)view.findViewById(android.R.id.widget_frame));
		widgetFrameView.setVisibility(View.VISIBLE);
		widgetFrameView.removeAllViews();
		LayoutInflater.from(getContext()).inflate(
				isEnabled()
					? R.layout.color_preference_thumbnail
					: R.layout.color_preference_thumbnail_disabled,
				widgetFrameView);
		return widgetFrameView.findViewById(R.id.thumbnail);
	}

	private Integer getPersistedIntDefaultOrNull() {
		return shouldPersist() && getSharedPreferences().contains(getKey())
				? Integer.valueOf(getPersistedInt(Color.GRAY))
				: defaultColor;
	}

	private void showColor(Integer color) {
		Integer thumbColor = color == null ? defaultColor : color;
		if (thumbnail != null) {
			thumbnail.setVisibility(thumbColor == null ? View.GONE : View.VISIBLE);
			thumbnail.findViewById(R.id.colorPreview).setBackgroundColor(thumbColor == null ? 0 : thumbColor);
		}
		if (noneSelectedSummaryText != null) {
			setSummary(thumbColor == null ? noneSelectedSummaryText : summaryText);
		}
	}

	private void removeSetting() {
		if (shouldPersist()) {
			getSharedPreferences()
					.edit()
					.remove(getKey())
					.apply();
		}
	}

	public void setColor(Integer color) {
		if (color == null) {
			removeSetting();
		}
		else {
			persistInt(color);
		}
		showColor(color);
	}

	public Integer getColor() {
		return getPersistedIntDefaultOrNull();
	}
}
