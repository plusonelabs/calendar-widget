package org.andstatus.todoagenda.prefs;

import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.EventProviderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventSourcesPreferencesFragment extends PreferenceFragmentCompat {
    private static final String TAG = EventSourcesPreferencesFragment.class.getSimpleName();
    private static final String SOURCE_ID = "sourceId";

    List<OrderedEventSource> savedActiveSources = Collections.emptyList();
    List<EventSource> clickedSources = new ArrayList<>();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_event_sources);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActiveSources();
    }

    private void loadActiveSources() {
        List<OrderedEventSource> activeSourcesNew = ApplicationPreferences.getActiveEventSources(getActivity());
        if(!(savedActiveSources.equals(activeSourcesNew))) {
            savedActiveSources = activeSourcesNew;
            Log.i(TAG, this.toString() + "\nLoaded " + savedActiveSources.size());
            showAllSources(activeSourcesNew);
        }
    }

    private void showAllSources(List<OrderedEventSource> activeSources) {
        getPreferenceScreen().removeAll();
        List<EventSource> added = new ArrayList<>();
        for (OrderedEventSource saved: activeSources) {
            added.add(saved.source);
            addAsPreference(saved.source, true);
        }
        for (EventSource clicked : clickedSources) {
            if (!added.contains(clicked)) {
                added.add(clicked);
                addAsPreference(clicked, false);
            }
        }
        for (OrderedEventSource available : EventProviderType.getAvailableSources()) {
            if (!added.contains(available.source)) {
                added.add(available.source);
                addAsPreference(available.source, false);
            }
        }
    }

    private void addAsPreference(EventSource source, boolean isChecked ) {
        CheckBoxPreference checkboxPref = new CheckBoxPreference(getActivity());
        checkboxPref.setTitle((source.isAvailable ? "" : getText(R.string.not_found) + ": ") + source.getTitle());
        checkboxPref.setSummary(source.getSummary());
        checkboxPref.setIcon(createDrawable(source.providerType.isCalendar, source.getColor()));
        checkboxPref.getExtras().putString(SOURCE_ID, source.toStoredString());
        getPreferenceScreen().addPreference(checkboxPref);
        checkboxPref.setChecked(isChecked);
    }

    private Drawable createDrawable(boolean isCalendar, int color) {
        Drawable drawable = getResources().getDrawable(
                isCalendar ? R.drawable.prefs_calendar_entry : R.drawable.task_icon
        );
        drawable.setColorFilter(new LightingColorFilter(0x0, color));
        return drawable;
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
            List<OrderedEventSource> selectedSources = getSelectedSources();
            Log.i(TAG, this.toString() + "\nSaving " + selectedSources.size());
            ApplicationPreferences.setActiveEventSources(getActivity(), selectedSources);
            savedActiveSources = selectedSources;
        }
        loadSelectedInOtherInstances();
    }

    private List<OrderedEventSource> getSelectedSources() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int prefCount = preferenceScreen.getPreferenceCount();
        List<EventSource> checkedSources = getCheckedSources(preferenceScreen, prefCount);
        List<EventSource> clickedSelectedSources = new ArrayList<>();
        for (EventSource clicked : clickedSources) {
            if (checkedSources.contains(clicked)) {
                checkedSources.remove(clicked);
                clickedSelectedSources.add(clicked);
            }
        }
        // Previously selected sources are first
        List<OrderedEventSource> selectedSources = OrderedEventSource.fromSources(checkedSources);
        // Then recently selected sources go
        return OrderedEventSource.addAll(selectedSources, clickedSelectedSources);
    }

    private List<EventSource> getCheckedSources(PreferenceScreen preferenceScreen, int prefCount) {
        List<EventSource> checkedSources = new ArrayList<>();
        for (int i = 0; i < prefCount; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBox = (CheckBoxPreference) preference;
                if (checkBox.isChecked()) {
                    EventSource eventSource = EventSource.fromStoredString(checkBox.getExtras().getString(SOURCE_ID));
                    if (eventSource != EventSource.EMPTY) {
                        checkedSources.add(eventSource);
                    }
                }
            }
        }
        return checkedSources;
    }

    private void loadSelectedInOtherInstances() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof EventSourcesPreferencesFragment && fragment != this) {
                    Log.i(TAG, this.toString() + "\nFound loaded " + fragment);
                    ((EventSourcesPreferencesFragment) fragment).loadActiveSources();
                }
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof CheckBoxPreference) {
            EventSource source = EventSource.fromStoredString(preference.getExtras().getString(SOURCE_ID));
            clickedSources.remove(source);
            clickedSources.add(source); // last clicked is the last in the list
            showAllSources(getSelectedSources());
        }
        return super.onPreferenceTreeClick(preference);
    }
}
