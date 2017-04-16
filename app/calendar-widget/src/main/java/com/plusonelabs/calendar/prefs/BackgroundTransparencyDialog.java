package com.plusonelabs.calendar.prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.plusonelabs.calendar.R;

public class BackgroundTransparencyDialog extends DialogFragment {

    private ColorPicker picker;
    private String prefKey;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.background_color, null);
        picker = (ColorPicker) layout.findViewById(R.id.background_color_picker);
        picker.addSVBar((SVBar) layout.findViewById(R.id.background_color_svbar));
        picker.addOpacityBar((OpacityBar) layout.findViewById(R.id.background_color_opacitybar));
        prefKey = getTag().equals(ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR)
                ? ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR
                : ApplicationPreferences.PREF_BACKGROUND_COLOR;
        int color = ApplicationPreferences.getInt(getActivity(), prefKey,
                getTag().equals(ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR)
                        ? ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR_DEFAULT
                        : ApplicationPreferences.PREF_BACKGROUND_COLOR_DEFAULT);
        // android.util.Log.v("Color", "key:" + prefKey + "; color:0x" + Integer.toString(color, 16));
        picker.setColor(color);
        picker.setOldCenterColor(color);
        return createDialog(layout);
    }

    private Dialog createDialog(View layout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTag().equals(ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR)
                ? R.string.appearance_past_events_background_color_title
                : R.string.appearance_background_color_title);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                ApplicationPreferences.setInt(getActivity(), prefKey, picker.getColor());
            }
        });
        return builder.create();
    }

}
