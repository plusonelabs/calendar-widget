package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.andstatus.todoagenda.R;

public class LayoutPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        showEventEntryLayout();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void showEventEntryLayout() {
        Preference preference = findPreference(ApplicationPreferences.PREF_EVENT_ENTRY_LAYOUT);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getEventEntryLayout(getActivity()).summaryResId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case ApplicationPreferences.PREF_EVENT_ENTRY_LAYOUT:
                showEventEntryLayout();
                break;
            default:
                break;
        }
    }
}