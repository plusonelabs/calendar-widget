package com.plusonelabs.calendar.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.calendar.KeywordsFilter;

public class EventFiltersPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void showEventsEnded() {
        ListPreference preference = (ListPreference) findPreference(ApplicationPreferences.PREF_EVENTS_ENDED);
        preference.setSummary(preference.getEntry());
    }

    private void showEvenRange() {
        ListPreference preference = (ListPreference) findPreference(ApplicationPreferences.PREF_EVENT_RANGE);
        preference.setSummary(preference.getEntry());
    }

    private void showHideBasedOnKeywords() {
        EditTextPreference preference = (EditTextPreference) findPreference(ApplicationPreferences.PREF_HIDE_BASED_ON_KEYWORDS);
        KeywordsFilter filter = new KeywordsFilter(preference.getText());
        if (filter.isEmpty()) {
            preference.setSummary(R.string.this_option_is_turned_off);
        } else {
            preference.setSummary(filter.toString());
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        EventAppWidgetProvider.updateEventList(getActivity());
        EventAppWidgetProvider.updateAllWidgets(getActivity());
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showStatus();
    }
}