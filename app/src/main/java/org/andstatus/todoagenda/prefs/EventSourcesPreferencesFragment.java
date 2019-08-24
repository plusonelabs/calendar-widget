package org.andstatus.todoagenda.prefs;

import android.app.Fragment;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.EventProviderType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EventSourcesPreferencesFragment extends PreferenceFragment {
    private static final String SOURCE_ID = "sourceId";

    List<EventSource> savedActiveSources = Collections.emptyList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_event_sources);
        populateAvailableSources();
    }


    private void populateAvailableSources() {
        Collection<EventSource> availableSources = EventProviderType.getAvailableSources();
        for (EventSource source : availableSources) {
            CheckBoxPreference checkboxPref = new CheckBoxPreference(getActivity());
            checkboxPref.setTitle(source.getTitle());
            checkboxPref.setSummary(source.getSummary());
            checkboxPref.setIcon(createDrawable(source.providerType.isCalendar, source.getColor()));
            checkboxPref.getExtras().putString(SOURCE_ID, source.toStoredString());
            getPreferenceScreen().addPreference(checkboxPref);
        }
    }

    private Drawable createDrawable(boolean isCalendar, int color) {
        Drawable drawable = getResources().getDrawable(
                isCalendar ? R.drawable.prefs_calendar_entry : R.drawable.task_icon
        );
        drawable.setColorFilter(new LightingColorFilter(0x0, color));
        return drawable;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActiveSources();
    }

    private void loadActiveSources() {
        List<EventSource> activeSourcesNew = ApplicationPreferences.getActiveEventSources(getActivity());
        if(!(savedActiveSources.equals(activeSourcesNew))) {
            savedActiveSources = activeSourcesNew;
            Log.i(this.getClass().getSimpleName(), this.toString() + "\nLoaded " + savedActiveSources.size());
            showActiveSources();
        }
    }

    public void showActiveSources() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBox = (CheckBoxPreference) preference;
                EventSource eventSource = EventSource.fromStoredString(checkBox.getExtras().getString(SOURCE_ID));
                checkBox.setChecked(savedActiveSources.contains(eventSource));
            }
        }
    }

    @Override
    public void onPause() {
        if (!getSelectedSources().equals(savedActiveSources)) {
            saveSelectedSources();
        }
        super.onPause();
    }

    private static final Object setLock = new Object();
    public void saveSelectedSources() {
        synchronized (setLock) {
            List<EventSource> selectedSources = getSelectedSources();
            Log.i(this.getClass().getSimpleName(), this.toString() + "\nSaving " + selectedSources.size());
            ApplicationPreferences.setActiveEventSources(getActivity(), selectedSources);
            savedActiveSources = selectedSources;
        }
        loadSelectedInOtherInstances();
    }

    private List<EventSource> getSelectedSources() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int prefCount = preferenceScreen.getPreferenceCount();
        List<EventSource> selectedSources = new ArrayList<>();
        for (int i = 0; i < prefCount; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBox = (CheckBoxPreference) preference;
                if (checkBox.isChecked()) {
                    EventSource eventSource = EventSource.fromStoredString(checkBox.getExtras().getString(SOURCE_ID));
                    if (eventSource != EventSource.EMPTY) {
                        selectedSources.add(eventSource);
                    }
                }
            }
        }
        return selectedSources;
    }

    private void loadSelectedInOtherInstances() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (Fragment fragment: getActivity().getFragmentManager().getFragments()) {
                if (fragment instanceof EventSourcesPreferencesFragment && fragment != this) {
                    Log.i(this.getClass().getSimpleName(), this.toString() + "\nFound loaded " + fragment);
                    ((EventSourcesPreferencesFragment) fragment).loadActiveSources();
                }
            }
        }
    }
}
