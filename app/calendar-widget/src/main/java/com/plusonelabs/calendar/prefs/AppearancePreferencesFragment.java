package com.plusonelabs.calendar.prefs;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.R;

public class AppearancePreferencesFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_appearance);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference.getKey().equals(CalendarPreferences.PREF_SHOW_HEADER)) {
			if (preference instanceof CheckBoxPreference) {
				CheckBoxPreference checkPref = (CheckBoxPreference) preference;
				if (!checkPref.isChecked()) {
					Toast.makeText(getActivity(), R.string.appearance_display_header_warning,
							Toast.LENGTH_LONG).show();
				}
			}
		}
		if (preference.getKey().equals(CalendarPreferences.PREF_BACKGROUND_COLOR)) {
			new BackgroundTransparencyDialog().show(getFragmentManager(),
			        CalendarPreferences.PREF_BACKGROUND_COLOR);
		}
        if (preference.getKey().equals(CalendarPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR)) {
            new BackgroundTransparencyDialog().show(getFragmentManager(),
                    CalendarPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR);
        }
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onPause() {
		super.onPause();
		EventAppWidgetProvider.updateEventList(getActivity());
		EventAppWidgetProvider.updateAllWidgets(getActivity());
	}
}