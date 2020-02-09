package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatDialog;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatPreference;

import static org.andstatus.todoagenda.WidgetConfigurationActivity.FRAGMENT_TAG;

public class LayoutPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
        Preference preference = findPreference(InstanceSettings.PREF_EVENT_ENTRY_LAYOUT);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getEventEntryLayout(getActivity()).summaryResId);
        }
    }

    private void showWidgetHeaderLayout() {
        Preference preference = findPreference(InstanceSettings.PREF_WIDGET_HEADER_LAYOUT);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getWidgetHeaderLayout(getActivity()).summaryResId);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof DateFormatPreference) {
            dialogFragment = new DateFormatDialog((DateFormatPreference) preference);
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
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
            case InstanceSettings.PREF_EVENT_ENTRY_LAYOUT:
                showEventEntryLayout();
                break;
            case InstanceSettings.PREF_WIDGET_HEADER_LAYOUT:
                showWidgetHeaderLayout();
                break;
            default:
                break;
        }
    }
}