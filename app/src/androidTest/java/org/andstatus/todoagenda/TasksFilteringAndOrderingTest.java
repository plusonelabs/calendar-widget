package org.andstatus.todoagenda;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class TasksFilteringAndOrderingTest extends BaseWidgetTest {

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/356
     */
    @Test
    public void testNoFilters() throws IOException, JSONException {
        final String method = "testNoFilters";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.filter_tasks_308_no_filters);
        provider.addResults(inputs.getResults());

        playResults(method);
        assertEquals("Number of entries", 35, factory.getWidgetEntries().size());
    }
}
