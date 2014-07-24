package com.plusonelabs.calendar.prefs;

import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.R;

public class AppearancePreferencesFragment extends UniquePreferencesFragment {

	private static final String BACKGROUND_COLOR_DIALOG = "backgroundColorDialog";

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
            BackgroundTransparencyDialog btd = new BackgroundTransparencyDialog();
            Bundle args = new Bundle();
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, this.appWidgetId);
            btd.setArguments(args);
            btd.show(getFragmentManager(), BACKGROUND_COLOR_DIALOG);
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