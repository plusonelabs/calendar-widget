package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.andstatus.todoagenda.EventAppWidgetProvider;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.task.TaskProvider;

public class TaskPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_task);
        setGrantPermissionVisibility(false);
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
                setGrantPermissionVisibility(true);
                break;
        }
    }

    private void showTaskSource() {
        ListPreference preference = (ListPreference) findPreference(ApplicationPreferences.PREF_TASK_SOURCE);
        preference.setSummary(preference.getEntry());
    }

    private void setGrantPermissionVisibility(boolean forceDisplay) {
        TaskProvider taskProvider = new TaskProvider(getActivity(), ApplicationPreferences.getWidgetId(getActivity()));
        if (taskProvider.hasPermission()) {
            Preference preference = findPreference("grantTaskPermission");
            if (preference != null) {
                PreferenceScreen screen = getPreferenceScreen();
                screen.removePreference(preference);
            }
        } else {
            if (forceDisplay) {
                getActivity().recreate();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case ApplicationPreferences.KEY_PREF_GRANT_TASK_PERMISSION:
                requestTaskPermission();
                return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void requestTaskPermission() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), ApplicationPreferences.getWidgetId(getActivity()));
        taskProvider.requestPermission(getActivity());
    }

    public void gotPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        setGrantPermissionVisibility(false);
    }
}