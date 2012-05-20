package com.plusonelabs.calendar;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CalendarConfigurationActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences_header, target);
	}
}