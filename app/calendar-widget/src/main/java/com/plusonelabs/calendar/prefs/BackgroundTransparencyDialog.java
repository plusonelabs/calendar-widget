package com.plusonelabs.calendar.prefs;

import static com.plusonelabs.calendar.prefs.CalendarPreferences.*;
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

	private static final String PERCENT_STR = "%";
	private SeekBar slider;
	private TextView percentage;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.background_transparency, null);
		percentage = (TextView) layout.findViewById(R.id.background_transparency_percent);
		initSlider(layout);
		return createDialog(layout);
	}

	public void initSlider(View layout) {
		slider = (SeekBar) layout.findViewById(R.id.background_transparency_slider);
		slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				// nothing to do here
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// nothing to do here
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				percentage.setText(roundToFive(progress) + PERCENT_STR);
			}

		});
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		slider.setProgress(prefs.getInt(PREF_BACKGROUND_TRANSPARENCY,
				PREF_BACKGROUND_TRANSPARENCY_DEFAULT));
	}

	public Dialog createDialog(View layout) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.appearance_background_transparency_title);
		builder.setView(layout);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				Editor editor = prefs.edit();
				editor.putInt(PREF_BACKGROUND_TRANSPARENCY, roundToFive(slider.getProgress()));
				editor.commit();
			}
		});
		return builder.create();
	}

	private static int roundToFive(int x) {
		int diff = x % 5;
		return diff == 0 ? x :
			   diff < 3  ? x - diff :
						   x + (5 - diff);
	}
}
