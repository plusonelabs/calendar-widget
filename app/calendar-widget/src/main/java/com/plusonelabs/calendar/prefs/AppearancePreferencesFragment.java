package com.plusonelabs.calendar.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.R;

import java.util.TimeZone;

public class AppearancePreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_appearance);
	}

	@Override
	public void onResume() {
		super.onResume();
		showLockTimeZone(true);
        showEventEntryLayout();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

    private void showEventEntryLayout() {
        Preference preference = findPreference(CalendarPreferences.PREF_EVENT_ENTRY_LAYOUT);
        if (preference != null) {
            preference.setSummary(CalendarPreferences.getEventEntryLayout(getActivity()).summaryResId);
        }
    }

    private void showLockTimeZone(boolean setAlso) {
        CheckBoxPreference preference = (CheckBoxPreference) findPreference(CalendarPreferences.PREF_LOCK_TIME_ZONE);
        if (preference != null) {
            boolean isChecked = setAlso ? CalendarPreferences.isTimeZoneLocked(getActivity()) : preference.isChecked();
            if (setAlso && preference.isChecked() != isChecked) {
                preference.setChecked(isChecked);
            }
            preference.setSummary(
                    String.format(getText(
                            isChecked ? R.string.lock_time_zone_on_desc : R.string.lock_time_zone_off_desc
                    ).toString(), DateUtil.getCurrentTimeZone(getActivity()).getName(DateUtil.now().getMillis())));
        }
    }

    @Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case CalendarPreferences.PREF_SHOW_WIDGET_HEADER:
                if (preference instanceof CheckBoxPreference) {
                    CheckBoxPreference checkPref = (CheckBoxPreference) preference;
                    if (!checkPref.isChecked()) {
                        Toast.makeText(getActivity(), R.string.appearance_display_header_warning,
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case CalendarPreferences.PREF_BACKGROUND_COLOR:
                new BackgroundTransparencyDialog().show(getFragmentManager(),
                        CalendarPreferences.PREF_BACKGROUND_COLOR);
                break;
            case CalendarPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR:
                new BackgroundTransparencyDialog().show(getFragmentManager(),
                        CalendarPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR);
                break;
            case CalendarPreferences.PREF_LOCK_TIME_ZONE:
                if (preference instanceof CheckBoxPreference) {
                    CheckBoxPreference checkPref = (CheckBoxPreference) preference;
                    CalendarPreferences.setLockedTimeZoneId(getActivity(),
                            checkPref.isChecked() ? TimeZone.getDefault().getID() : "");
                    showLockTimeZone(false);
                }
                break;
            default:
                break;
        }
		return super.onPreferenceTreeClick(preferenceScreen, preference);
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
            case CalendarPreferences.PREF_EVENT_ENTRY_LAYOUT:
                showEventEntryLayout();
                break;
            default:
                break;
        }
    }
}