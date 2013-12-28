package com.plusonelabs.calendar.prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.plusonelabs.calendar.R;

import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_BACKGROUND_COLOR;
import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_BACKGROUND_COLOR_DEFAULT;

public class BackgroundTransparencyDialog extends DialogFragment {

    private ColorPicker picker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.background_color, null);
        picker = (ColorPicker) layout.findViewById(R.id.background_color_picker);
        picker.addSVBar((SVBar) layout.findViewById(R.id.background_color_svbar));
        picker.addOpacityBar((OpacityBar) layout.findViewById(R.id.background_color_opacitybar));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int color = prefs.getInt(PREF_BACKGROUND_COLOR, PREF_BACKGROUND_COLOR_DEFAULT);
        picker.setColor(color);
        picker.setOldCenterColor(color);
        return createDialog(layout);
    }


    private Dialog createDialog(View layout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.appearance_background_color_title);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				Editor editor = prefs.edit();
                editor.putInt(PREF_BACKGROUND_COLOR, picker.getColor());
                editor.commit();
            }
		});
		return builder.create();
	}

}
