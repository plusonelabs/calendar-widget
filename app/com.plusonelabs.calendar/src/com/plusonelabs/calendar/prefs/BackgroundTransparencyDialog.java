package com.plusonelabs.calendar.prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.plusonelabs.calendar.R;

public class BackgroundTransparencyDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.background_transparency, null);
		final SeekBar slider = (SeekBar) layout.findViewById(R.id.background_transparency_slider);
		final TextView percentage = (TextView) layout
				.findViewById(R.id.background_transparency_percent);
		slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				// nothing to do here
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// nothing to do here
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				percentage.setText(roundToTen(progress) + "%");
			}

		});
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		slider.setProgress((int) (prefs.getFloat(ICalendarPreferences.PREF_BACKGROUND_TRANSPARENCY,
				0.5f) * 100));
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.appearance_background_transparency_title);
		builder.setView(layout);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Editor editor = prefs.edit();
				editor.putFloat(ICalendarPreferences.PREF_BACKGROUND_TRANSPARENCY,
						roundToTen(slider.getProgress()) / 100f);
				editor.commit();

			}
		});
		return builder.create();
	}

	private int roundToTen(int x) {
		return (x + 5) / 10 * 10;
	}
}
