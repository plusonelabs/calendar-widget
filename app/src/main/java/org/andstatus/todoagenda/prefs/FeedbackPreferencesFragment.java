package org.andstatus.todoagenda.prefs;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.calendar.CalendarQueryResultsStorage;

public class FeedbackPreferencesFragment extends PreferenceFragment {

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case ApplicationPreferences.KEY_SHARE_EVENTS_FOR_DEBUGGING:
                CalendarQueryResultsStorage.shareEventsForDebugging(getActivity(),
                        ApplicationPreferences.getWidgetId(getActivity()));
            default:
                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_feedback);
    }
}