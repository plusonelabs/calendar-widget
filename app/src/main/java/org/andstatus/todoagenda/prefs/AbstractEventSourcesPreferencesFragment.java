package org.andstatus.todoagenda.prefs;

import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.andstatus.todoagenda.EventAppWidgetProvider;
import org.andstatus.todoagenda.R;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

abstract class AbstractEventSourcesPreferencesFragment extends PreferenceFragment {

    private static final String SOURCE_ID = "sourceId";

    private Set<String> initialActiveSources;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_calendars);
        initialActiveSources = fetchInitialActiveSources();
        populatePreferenceScreen(initialActiveSources);
    }

    protected abstract Set<String> fetchInitialActiveSources();

    private void populatePreferenceScreen(Set<String> activeCalendars) {
        Collection<EventSource> availableSources = fetchAvailableSources();
        for (EventSource source : availableSources) {
            CheckBoxPreference checkboxPref = new CheckBoxPreference(getActivity());
            checkboxPref.setTitle(source.getTitle());
            checkboxPref.setSummary(source.getSummary());
            checkboxPref.setIcon(createDrawable(source.getColor()));
            int sourceId = source.getId();
            checkboxPref.getExtras().putInt(SOURCE_ID, sourceId);
            checkboxPref.setChecked(activeCalendars.isEmpty()
                    || activeCalendars.contains(String.valueOf(sourceId)));
            getPreferenceScreen().addPreference(checkboxPref);
        }
    }

    protected abstract Collection<EventSource> fetchAvailableSources();

    @Override
    public void onPause() {
        super.onPause();
        Set<String> selectedSources = getSelectedSources();
        if (!selectedSources.equals(initialActiveSources)) {
            storeSelectedSources(selectedSources);
            EventAppWidgetProvider.updateEventList(getActivity());
        }
    }

    protected abstract void storeSelectedSources(Set<String> selectedSources);

    private Set<String> getSelectedSources() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int prefCount = preferenceScreen.getPreferenceCount();
        Set<String> prefValues = new HashSet<>();
        for (int i = 0; i < prefCount; i++) {
            Preference pref = preferenceScreen.getPreference(i);
            if (pref instanceof CheckBoxPreference) {
                CheckBoxPreference checkPref = (CheckBoxPreference) pref;
                if (checkPref.isChecked()) {
                    prefValues.add(String.valueOf(checkPref.getExtras().getInt(SOURCE_ID)));
                }
            }
        }
        return prefValues;
    }

    private Drawable createDrawable(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.prefs_calendar_entry);
        drawable.setColorFilter(new LightingColorFilter(0x0, color));
        return drawable;
    }
}
