package org.andstatus.todoagenda.prefs;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;

import java.util.Optional;

public class RootFragment extends PreferenceFragmentCompat {

    @Override
    public void onResume() {
        super.onResume();
        Optional.ofNullable(getActivity())
                .ifPresent(a -> a.setTitle(ApplicationPreferences.getWidgetInstanceName(a)));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_root);
    }
}