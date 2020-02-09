package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;

public class EventFiltersPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_event_filters);
    }

    @Override
    public void onResume() {
        super.onResume();
        showStatus();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void showStatus() {
        showEventsEnded();
        showEvenRange();
        showHideBasedOnKeywords();
        showTaskScheduling();
        showTasksWithoutDates();
        showFilterMode();
    }

    private void showEventsEnded() {
        ListPreference preference = (ListPreference) findPreference(InstanceSettings.PREF_EVENTS_ENDED);
        preference.setSummary(preference.getEntry());
    }

    private void showEvenRange() {
        ListPreference preference = (ListPreference) findPreference(InstanceSettings.PREF_EVENT_RANGE);
        preference.setSummary(preference.getEntry());
    }

    private void showHideBasedOnKeywords() {
        EditTextPreference preference = (EditTextPreference) findPreference(InstanceSettings.PREF_HIDE_BASED_ON_KEYWORDS);
        KeywordsFilter filter = new KeywordsFilter(preference.getText());
        if (filter.isEmpty()) {
            preference.setSummary(R.string.this_option_is_turned_off);
        } else {
            preference.setSummary(filter.toString());
        }
    }

    private void showTaskScheduling() {
        Preference preference = findPreference(InstanceSettings.PREF_TASK_SCHEDULING);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getTaskScheduling(getActivity()).valueResId);
        }
    }

    private void showTasksWithoutDates() {
        Preference preference = findPreference(InstanceSettings.PREF_TASK_WITHOUT_DATES);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getTasksWithoutDates(getActivity()).valueResId);
        }
    }

    private void showFilterMode() {
        Preference preference = findPreference(InstanceSettings.PREF_FILTER_MODE);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getFilterMode(getActivity()).valueResId);
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showStatus();
    }
}