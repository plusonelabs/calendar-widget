package com.plusonelabs.calendar.prefs;

import com.plusonelabs.calendar.R;
import com.plusonelabs.calendar.R.xml;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class MainPreferencesFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences_main);
	}
}