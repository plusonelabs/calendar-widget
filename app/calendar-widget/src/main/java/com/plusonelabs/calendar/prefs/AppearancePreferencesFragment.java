package com.plusonelabs.calendar.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.MainActivity;
import com.plusonelabs.calendar.R;

import org.joda.time.DateTimeZone;

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
        showWidgetInstanceName();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void showEventEntryLayout() {
        Preference preference = findPreference(ApplicationPreferences.PREF_EVENT_ENTRY_LAYOUT);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getEventEntryLayout(getActivity()).summaryResId);
        }
    }

    private void showLockTimeZone(boolean setAlso) {
        CheckBoxPreference preference = (CheckBoxPreference) findPreference(ApplicationPreferences.PREF_LOCK_TIME_ZONE);
        if (preference != null) {
            boolean isChecked = setAlso ? ApplicationPreferences.isTimeZoneLocked(getActivity()) : preference.isChecked();
            if (setAlso && preference.isChecked() != isChecked) {
                preference.setChecked(isChecked);
            }
            DateTimeZone timeZone = DateTimeZone.forID(DateUtil.validatedTimeZoneId(isChecked ?
                    ApplicationPreferences.getLockedTimeZoneId(getActivity()) : TimeZone.getDefault().getID()));
            preference.setSummary(String.format(
                    getText(isChecked ? R.string.lock_time_zone_on_desc : R.string.lock_time_zone_off_desc).toString(),
                    timeZone.getName(DateUtil.now(timeZone).getMillis()))
            );
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case ApplicationPreferences.PREF_BACKGROUND_COLOR:
                new BackgroundTransparencyDialog().show(getFragmentManager(),
                        ApplicationPreferences.PREF_BACKGROUND_COLOR);
                break;
            case ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR:
                new BackgroundTransparencyDialog().show(getFragmentManager(),
                        ApplicationPreferences.PREF_PAST_EVENTS_BACKGROUND_COLOR);
                break;
            case ApplicationPreferences.PREF_LOCK_TIME_ZONE:
                if (preference instanceof CheckBoxPreference) {
                    CheckBoxPreference checkPref = (CheckBoxPreference) preference;
                    ApplicationPreferences.setLockedTimeZoneId(getActivity(),
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
            case ApplicationPreferences.PREF_EVENT_ENTRY_LAYOUT:
                showEventEntryLayout();
                break;
            case ApplicationPreferences.PREF_WIDGET_INSTANCE_NAME:
                getActivity().finish();
                startActivity(MainActivity.intentToConfigure(getActivity(), ApplicationPreferences
                        .getWidgetId(getActivity())));
                break;
            default:
                break;
        }
    }

    private void showWidgetInstanceName() {
        Preference preference = findPreference(ApplicationPreferences.PREF_WIDGET_INSTANCE_NAME);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getWidgetInstanceName(getActivity()));
        }
    }
}