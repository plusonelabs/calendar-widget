package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.provider.EventProviderType;

import java.util.Collection;
import java.util.Set;

public class TaskListsPreferencesFragment extends AbstractEventSourcesPreferencesFragment {

    @Override
    protected Set<String> fetchInitialActiveSources() {
        return ApplicationPreferences.getActiveTaskLists(getActivity());
    }

    @Override
    protected Collection<EventSource> fetchAvailableSources() {
        return EventProviderType.fetchAvailableSources(false);
    }

    @Override
    protected void storeSelectedSources(Set<String> selectedSources) {
        ApplicationPreferences.setActiveTaskLists(getActivity(), selectedSources);
    }
}
