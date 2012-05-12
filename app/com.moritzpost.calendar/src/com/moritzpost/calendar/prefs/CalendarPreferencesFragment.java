package com.moritzpost.calendar.prefs;

import com.moritzpost.calendar.R;
import com.moritzpost.calendar.R.xml;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class CalendarPreferencesFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences_main);
	}
}