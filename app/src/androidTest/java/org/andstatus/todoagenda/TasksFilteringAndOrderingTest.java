package org.andstatus.todoagenda;

import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.TaskScheduling;
import org.andstatus.todoagenda.prefs.TasksWithoutDates;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryPosition;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;

/**
 * See https://github.com/plusonelabs/calendar-widget/issues/356
 * @author yvolk@yurivolkov.com
 */
public class TasksFilteringAndOrderingTest extends BaseWidgetTest {

    @Test
    public void testDateDueNoFilters() throws IOException, JSONException {
        final String method = "testDateDueNoFilters";
        List<String> names = Arrays.asList(
                WidgetEntryPosition.DAY_HEADER.value,
                "task11 ", "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "a Today's event at midnight", "Today's event at 4AM", "Today's event at 8:05",
                "task3 ",
                "Today's event later at 9:05PM",
                "task1 ", "task10 ", "task9 ",
                "", "task6 ", "task7 ", "task12 ",
                "", "Test event that",
                "", "task17 ",
                "", "task16 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task14 ", "task8 ", "task4 ", "task2 ", "task15 ");

        Consumer<InstanceSettings> setter = settings ->
                settings.setTaskScheduling(TaskScheduling.DATE_DUE)
                        .setTaskWithoutDates(TasksWithoutDates.END_OF_TODAY)
                        .setFilterMode(FilterMode.NO_FILTERING);

        assertOneCase(method, setter, names);
    }

    @Test
    public void testDateStartedNoFilters() throws IOException, JSONException {
        final String method = "testDateStartedNoFilters";
        List<String> names = Arrays.asList(
                WidgetEntryPosition.DAY_HEADER.value,
                "task11 ", "task5 ",
                WidgetEntryPosition.DAY_HEADER.value,
                "task3 ", "task1 ", "task6 ", "task16 ", "task4 ", "task2 ",
                "a Today's event at midnight", "Today's event at 4AM",
                "task12 ", "task7 ",
                "Today's event at 8:05",
                "Today's event later at 9:05PM",
                "task10 ", "task9 ",
                "", "Test event that",
                "", "task8 ",
                "", "task17 ",
                WidgetEntryPosition.END_OF_LIST_HEADER.value,
                "task14 ", "task15 ");

        Consumer<InstanceSettings> setter = settings ->
                settings.setTaskScheduling(TaskScheduling.DATE_STARTED)
                        .setTaskWithoutDates(TasksWithoutDates.END_OF_TODAY)
                        .setFilterMode(FilterMode.NO_FILTERING);

        assertOneCase(method, setter, names);
    }

    private void assertOneCase(String method, Consumer<InstanceSettings> setter, List<String> names) throws IOException, JSONException {
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.filter_tasks_308_no_filters);
        provider.addResults(inputs.getResults());

        setter.accept(getSettings());

        playResults(method);

        List<? extends WidgetEntry> widgetEntries = factory.getWidgetEntries();
        for (int ind = 0; ind < names.size(); ind++) {
            WidgetEntryPosition entryPosition = WidgetEntryPosition.fromValue(names.get(ind));
            switch (entryPosition) {
                case UNKNOWN:
                    assertThat("ind=" + ind, widgetEntries.get(ind).getTitle(), startsWith(names.get(ind)));
                    break;
                default:
                    assertEquals("ind=" + ind, widgetEntries.get(ind).entryPosition, entryPosition);
                    break;
            }
        }
    }
}