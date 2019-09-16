package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.andstatus.todoagenda.EndedSomeTimeAgo;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.TextShading;
import org.andstatus.todoagenda.widget.TimeSection;

public class ColorsPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean hideDayHeader;
    private boolean hidePast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_colors);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        hideDayHeader = !ApplicationPreferences.getShowDayHeaders(getActivity());
        hidePast = !ApplicationPreferences.getShowPastEventsWithDefaultColor(getActivity()) &&
                ApplicationPreferences.getEventsEnded(getActivity()) == EndedSomeTimeAgo.NONE &&
                ApplicationPreferences.noTaskSources(getActivity());
        showShadings();
    }

    private void showShadings() {
        if (hidePast) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference preference = findPreference(TimeSection.PAST.preferenceCategoryKey);
            if (screen != null && preference != null) {
                screen.removePreference(preference);
            }
        }
        for (TextShadingPref shadingPref : TextShadingPref.values()) {
            showShading(shadingPref);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        showShadings();
    }

    private void showShading(TextShadingPref prefs) {
        ListPreference preference = (ListPreference) findPreference(prefs.preferenceName);
        if (preference != null) {
            TextShading shading = TextShading.fromName(preference.getValue(), prefs.defaultShading);
            if (hideDayHeader && prefs.dependsOnDayHeader) {
                PreferenceCategory category = (PreferenceCategory) findPreference(prefs.timeSection.preferenceCategoryKey);
                if (category != null) {
                    category.removePreference(preference);
                }
            } else {
                preference.setSummary(getActivity().getString(shading.titleResId));
            }
        }
    }
}