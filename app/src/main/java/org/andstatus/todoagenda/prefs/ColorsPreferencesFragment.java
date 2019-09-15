package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.TextShading;

public class ColorsPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_colors);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        showShadings();
    }

    private void showShadings() {
        for (TextShadingPref shadingPref : TextShadingPref.values()) {
            showShading(shadingPref);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showShadings();
    }

    private void showShading(TextShadingPref prefs) {
        ListPreference preference = (ListPreference) findPreference(prefs.preferenceName);
        if (preference != null) {
            TextShading shading = TextShading.fromName(preference.getValue(), prefs.defaultShading);
            preference.setSummary(getActivity().getString(shading.titleResId));
        }
    }
}