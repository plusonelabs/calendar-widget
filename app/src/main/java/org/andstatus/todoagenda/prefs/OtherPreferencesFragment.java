package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

public class OtherPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_other);
    }

    @Override
    public void onResume() {
        super.onResume();
        showLockTimeZone(true);
        showWidgetInstanceName();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void showLockTimeZone(boolean setAlso) {
        CheckBoxPreference preference = (CheckBoxPreference) findPreference(InstanceSettings.PREF_LOCK_TIME_ZONE);
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
            case InstanceSettings.PREF_LOCK_TIME_ZONE:
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
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case InstanceSettings.PREF_WIDGET_INSTANCE_NAME:
                getActivity().finish();
                startActivity(MainActivity.intentToConfigure(getActivity(), ApplicationPreferences
                        .getWidgetId(getActivity())));
                break;
            default:
                break;
        }
    }

    private void showWidgetInstanceName() {
        Preference preference = findPreference(InstanceSettings.PREF_WIDGET_INSTANCE_NAME);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getWidgetInstanceName(getActivity()));
        }
    }
}