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
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

/** AndroidX version created by yvolk@yurivolkov.com
 *   based on this answer: https://stackoverflow.com/a/53290775/297710
 *   and on the code of https://github.com/koji-1009/ChronoDialogPreference
*/
public class ColorPreferenceDialog extends PreferenceDialogFragmentCompat {
	private final ColorPreference preference;

	private ColorPickerView mPicker = null;

	public ColorPreferenceDialog(ColorPreference preference) {
		this.preference = preference;

		final Bundle b = new Bundle();
		b.putString(ARG_KEY, preference.getKey());
		setArguments(b);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Nexus 7 needs the keyboard hiding explicitly.
		// A flag on the activity in the manifest doesn't
		// apply to the dialog, so needs to be in code:
		Window window = getActivity().getWindow();
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	protected View onCreateDialogView(Context context) {
		final ColorPickerView picker = new ColorPickerView(getContext());

		picker.setColor(preference.getColor() == null ? Color.GRAY : preference.getColor());
		picker.showAlpha(preference.showAlpha);
		picker.showHex(preference.showHex);
		picker.showPreview(preference.showPreview);
		mPicker = picker;
		return mPicker;
	}

	@Override
	public void onStart() {
		super.onStart();

		AlertDialog dialog = (AlertDialog) getDialog();
		if (preference.selectNoneButtonText != null && preference.defaultColor != null && mPicker != null &&
				dialog != null) {
			// In order to prevent dialog from closing we setup its onLickListener this late
			Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
			if (neutralButton != null) {
				neutralButton.setOnClickListener(
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mPicker.setCurrentColor(preference.defaultColor);
							}
						});
			}
		}
	}

	@Override
	protected void onPrepareDialogBuilder(androidx.appcompat.app.AlertDialog.Builder builder) {
		if (preference.selectNoneButtonText != null) {
			builder.setNeutralButton(preference.selectNoneButtonText, null);
		}
	}

	@Override
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult && mPicker != null) {
			final int color = mPicker.getColor();
			if (preference.callChangeListener(color)) {
				preference.setColor(color);
			}
		}
	}
}