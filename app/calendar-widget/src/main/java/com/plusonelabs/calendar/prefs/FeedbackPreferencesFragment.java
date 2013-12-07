package com.plusonelabs.calendar.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.plusonelabs.calendar.R;

public class FeedbackPreferencesFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_feedback);
	}
}