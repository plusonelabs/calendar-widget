package com.plusonelabs.calendar.prefs;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.CalendarContract.Calendars;

import com.plusonelabs.calendar.EventAppWidgetProvider;
import com.plusonelabs.calendar.R;

import java.util.HashSet;
import java.util.Set;

public class CalendarPreferencesFragment extends PreferenceFragment {

    private static final String CALENDAR_ID = "calendarId";
    private static final String[] PROJECTION = new String[]{Calendars._ID,
            Calendars.CALENDAR_DISPLAY_NAME, Calendars.CALENDAR_COLOR,
            Calendars.ACCOUNT_NAME};
    private Set<String> initialActiveCalendars;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_calendars);
        initialActiveCalendars = ApplicationPreferences.getActiveCalendars(getActivity());
        populatePreferenceScreen(initialActiveCalendars);
    }

    private void populatePreferenceScreen(Set<String> activeCalendars) {
        Cursor cursor = createLoadedCursor();
        if (cursor == null) {
            return;
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            CheckBoxPreference checkboxPref = new CheckBoxPreference(getActivity());
            checkboxPref.setTitle(cursor.getString(1));
            checkboxPref.setSummary(cursor.getString(3));
            checkboxPref.setIcon(createDrawable(cursor.getInt(2)));
            int calendarId = cursor.getInt(0);
            checkboxPref.getExtras().putInt(CALENDAR_ID, calendarId);
            checkboxPref.setChecked(activeCalendars.isEmpty()
                    || activeCalendars.contains(String.valueOf(calendarId)));
            getPreferenceScreen().addPreference(checkboxPref);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        HashSet<String> selectedCalendars = getSelectedCalendars();
        if (!selectedCalendars.equals(initialActiveCalendars)) {
            ApplicationPreferences.setActiveCalendars(getActivity(), selectedCalendars);
            EventAppWidgetProvider.updateEventList(getActivity());
        }
    }

    private HashSet<String> getSelectedCalendars() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int prefCount = preferenceScreen.getPreferenceCount();
        HashSet<String> prefValues = new HashSet<>();
        for (int i = 0; i < prefCount; i++) {
            Preference pref = preferenceScreen.getPreference(i);
            if (pref instanceof CheckBoxPreference) {
                CheckBoxPreference checkPref = (CheckBoxPreference) pref;
                if (checkPref.isChecked()) {
                    prefValues.add(String.valueOf(checkPref.getExtras().getInt(CALENDAR_ID)));
                }
            }
        }
        return prefValues;
    }

    private Drawable createDrawable(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.prefs_calendar_entry);
        drawable.setColorFilter(new LightingColorFilter(0x0, color));
        return drawable;
    }

    private Cursor createLoadedCursor() {
        Uri.Builder builder = Calendars.CONTENT_URI.buildUpon();
        ContentResolver contentResolver = getActivity().getContentResolver();
        return contentResolver.query(builder.build(), PROJECTION, null, null, null);
    }
}
