package org.andstatus.todoagenda.prefs;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CalendarPreferencesFragment extends AbstractEventSourcesPreferencesFragment {

    private static final String[] PROJECTION = new String[]{Calendars._ID,
            Calendars.CALENDAR_DISPLAY_NAME, Calendars.CALENDAR_COLOR,
            Calendars.ACCOUNT_NAME};

    @Override
    protected Set<String> fetchInitialActiveSources() {
        return ApplicationPreferences.getActiveCalendars(getActivity());
    }

    @Override
    protected Collection<EventSource> fetchAvailableSources() {
        List<EventSource> eventSources = new ArrayList<>();

        Cursor cursor = createLoadedCursor();
        if (cursor == null) {
            return eventSources;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            EventSource source = new EventSource(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(3), cursor.getInt(2));
            eventSources.add(source);
        }
        return eventSources;
    }

    private Cursor createLoadedCursor() {
        Uri.Builder builder = Calendars.CONTENT_URI.buildUpon();
        ContentResolver contentResolver = getActivity().getContentResolver();
        return contentResolver.query(builder.build(), PROJECTION, null, null, null);
    }

    @Override
    protected void storeSelectedSources(Set<String> selectedSources) {
        ApplicationPreferences.setActiveCalendars(getActivity(), selectedSources);
    }
}
