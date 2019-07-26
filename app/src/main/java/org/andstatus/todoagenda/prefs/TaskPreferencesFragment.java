package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.andstatus.todoagenda.EventAppWidgetProvider;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.task.TaskProvider;

import java.util.Collections;

public class TaskPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_ACTIVE_TASK_LISTS_BUTTON = "activeTaskListsButton";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_task);
    }

    @Override
    public void onResume() {
        super.onResume();
        showTaskSource();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        EventAppWidgetProvider.updateEventList(getActivity());
        EventAppWidgetProvider.updateAllWidgets(getActivity());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case ApplicationPreferences.PREF_TASK_SOURCE:
                showTaskSource();
                setTaskListState();
                clearTasksLists();
                break;
        }
    }

    private void showTaskSource() {
        ListPreference preference = (ListPreference) findPreference(ApplicationPreferences.PREF_TASK_SOURCE);
        preference.setSummary(preference.getEntry());
    }

    private void setTaskListState() {
        Preference taskListButton = findPreference(PREF_ACTIVE_TASK_LISTS_BUTTON);
        taskListButton.setEnabled(!ApplicationPreferences.getTaskSource(getActivity()).equals(TaskProvider.PROVIDER_NONE));
    }

    private void clearTasksLists() {
        ApplicationPreferences.setActiveTaskLists(getActivity(), Collections.<String>emptySet());
    }
}