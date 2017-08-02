package com.plusonelabs.calendar.prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.larswerkman.holocolorpicker.OpacityBar;
import com.plusonelabs.calendar.R;

import static com.plusonelabs.calendar.prefs.ApplicationPreferences.PREF_EVENT_BACKGROUND_COLOR_OVERRIDE_OPACITY;
import static com.plusonelabs.calendar.prefs.ApplicationPreferences.PREF_EVENT_BACKGROUND_COLOR_OVERRIDE_OPACITY_DEFAULT;

public class EventBackgroundOverrideTransparencyDialog extends DialogFragment {

    private String prefKey;
    private OpacityBar opacityBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.event_color_override_opacity, null);
        opacityBar = (OpacityBar) layout.findViewById(R.id.background_color_opacitybar);
        prefKey = PREF_EVENT_BACKGROUND_COLOR_OVERRIDE_OPACITY;

        int alpha = ApplicationPreferences.getInt(getActivity(),
                prefKey, PREF_EVENT_BACKGROUND_COLOR_OVERRIDE_OPACITY_DEFAULT);
        Log.v("Color", "fetching alpha: " + alpha);
        opacityBar.setOpacity(alpha);
        return createDialog(layout);
    }

    private Dialog createDialog(View layout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.appearance_background_event_color_override_opacity_title);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                int alpha = Color.alpha(opacityBar.getColor());
                Log.v("Color", "Saving alpha: " + alpha);
                ApplicationPreferences.setInt(getActivity(), prefKey, alpha);
            }
        });
        return builder.create();
    }

}
