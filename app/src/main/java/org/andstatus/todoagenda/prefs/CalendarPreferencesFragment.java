package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.provider.EventProviderType;

import java.util.Collection;
import java.util.Set;

public class CalendarPreferencesFragment extends AbstractEventSourcesPreferencesFragment {

    @Override
    protected Set<String> fetchInitialActiveSources() {
        return ApplicationPreferences.getActiveCalendars(getActivity());
    }

    @Override
    protected Collection<EventSource> fetchAvailableSources() {
        return EventProviderType.fetchAvailableSources(true);
    }

    @Override
    protected void storeSelectedSources(Set<String> selectedSources) {
        ApplicationPreferences.setActiveCalendars(getActivity(), selectedSources);
    }
}
