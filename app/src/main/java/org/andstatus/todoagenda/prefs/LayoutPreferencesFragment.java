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
        showWidgetHeaderLayout();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void showEventEntryLayout() {
        Preference preference = findPreference(ApplicationPreferences.PREF_EVENT_ENTRY_LAYOUT);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getEventEntryLayout(getActivity()).summaryResId);
        }
    }

    private void showWidgetHeaderLayout() {
        Preference preference = findPreference(ApplicationPreferences.PREF_WIDGET_HEADER_LAYOUT);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getWidgetHeaderLayout(getActivity()).summaryResId);
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
            case ApplicationPreferences.PREF_WIDGET_HEADER_LAYOUT:
                showWidgetHeaderLayout();
                break;
            default:
                break;
        }
    }
}