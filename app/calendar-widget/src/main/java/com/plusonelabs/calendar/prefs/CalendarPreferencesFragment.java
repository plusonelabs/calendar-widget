package com.plusonelabs.calendar.prefs;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

import static com.plusonelabs.calendar.prefs.CalendarPreferences.PREF_ACTIVE_CALENDARS;

public class CalendarPreferencesFragment extends PreferenceFragment {

	private static final String CALENDAR_ID = "calendarId";
	private static final String[] PROJECTION = new String[] { Calendars._ID,
			Calendars.CALENDAR_DISPLAY_NAME, Calendars.CALENDAR_COLOR,
			Calendars.ACCOUNT_NAME };
	private Set<String> initialActiveCalendars;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_calendars);
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		initialActiveCalendars = prefs.getStringSet(PREF_ACTIVE_CALENDARS,
				null);
		populatePreferenceScreen(initialActiveCalendars);
	}

    private void populatePreferenceScreen(Set<String> activeCalendars) {
        Cursor cursor = createLoadedCursor();
		if (cursor == null) {
			return;
		}
		String account_prefix = getResources().getString(R.string.account_prefix);
		if ( account_prefix != null && !account_prefix.isEmpty() )
			account_prefix += " ";

		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			CheckBoxPreference checkboxPref = new CheckBoxPreference(getActivity());
			checkboxPref.setTitle(cursor.getString(1));
			checkboxPref.setSummary(account_prefix + cursor.getString(3));
			checkboxPref.setIcon(createDrawable(cursor.getInt(2)));
			int calendarId = cursor.getInt(0);
			checkboxPref.getExtras().putInt(CALENDAR_ID, calendarId);
			checkboxPref.setChecked(activeCalendars == null
					|| activeCalendars.contains(String.valueOf(calendarId)));
			getPreferenceScreen().addPreference(checkboxPref);
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		HashSet<String> selectedCalendars = getSelectedCalenders();
		if (!selectedCalendars.equals(initialActiveCalendars)) {
			persistSelectedCalendars(selectedCalendars);
			EventAppWidgetProvider.updateEventList(getActivity());
		}
	}

    private void persistSelectedCalendars(HashSet<String> prefValues) {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		Editor editor = prefs.edit();
        editor.putStringSet(PREF_ACTIVE_CALENDARS, prefValues);
		editor.commit();
	}

    private HashSet<String> getSelectedCalenders() {
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
