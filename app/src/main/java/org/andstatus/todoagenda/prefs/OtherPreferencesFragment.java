package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
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
        showSnapshotMode();
        showRefreshPeriod();
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
                    timeZone.getName(DateTime.now(timeZone).getMillis()))
            );
        }
    }

    private void showSnapshotMode() {
        Preference preference = findPreference(InstanceSettings.PREF_SNAPSHOT_MODE);
        if (preference == null) return;

        SnapshotMode snapshotMode = ApplicationPreferences.getSnapshotMode(getActivity());
        if (snapshotMode == SnapshotMode.LIVE_DATA) {
            preference.setSummary(snapshotMode.valueResId);
        } else {
            InstanceSettings settings = getSettings();
            preference.setSummary(String.format(
                    getText(snapshotMode.valueResId).toString(),
                    DateUtil.createDateString(getSettings(), settings.clock().now()) + " " +
                    DateUtil.formatTime(this::getSettings, settings.clock().now())
            ));
        }
    }

    private InstanceSettings getSettings() {
        int widgetId = ApplicationPreferences.getWidgetId(getActivity());
        return AllSettings.instanceFromId(getActivity(), widgetId);
    }

    private void showRefreshPeriod() {
        EditTextPreference preference = (EditTextPreference) findPreference(InstanceSettings.PREF_REFRESH_PERIOD_MINUTES);

        if (preference != null) {
            int value = ApplicationPreferences.getRefreshPeriodMinutes(getActivity());
            preference.setSummary(String.format(getText(R.string.refresh_period_minutes_desc).toString(), value));
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
            case InstanceSettings.PREF_REFRESH_PERIOD_MINUTES:
                showRefreshPeriod();
                break;
            case InstanceSettings.PREF_SNAPSHOT_MODE:
                SnapshotMode snapshotMode = ApplicationPreferences.getSnapshotMode(getActivity());
                if (snapshotMode != SnapshotMode.LIVE_DATA) {
                    InstanceSettings settings = getSettings();
                    if (!settings.hasResults()) {
                        settings.setResultsStorage(QueryResultsStorage.getNewResults(getActivity(), settings.widgetId));
                        settings.clock().setSnapshotMode(snapshotMode);
                        settings.save("newResultsForSnapshotMode");
                    }
                }
                showSnapshotMode();
                break;
            default:
                break;
        }
    }

    private void showWidgetInstanceName() {
        Preference preference = findPreference(InstanceSettings.PREF_WIDGET_INSTANCE_NAME);
        if (preference != null) {
            preference.setSummary(ApplicationPreferences.getWidgetInstanceName(getActivity()) +
                    " (id:" + ApplicationPreferences.getWidgetId(getActivity()) +")");
        }
    }
}