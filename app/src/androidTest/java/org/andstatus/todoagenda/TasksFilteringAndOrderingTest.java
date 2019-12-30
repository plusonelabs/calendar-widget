package org.andstatus.todoagenda;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

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
        QueryResultsStorage inputs = provider.loadResultsAndSettings(InstrumentationRegistry.getInstrumentation().getContext(),
                org.andstatus.todoagenda.tests.R.raw.filter_tasks_308_no_filters);
        provider.addResults(inputs.getResults());
        Log.d(method, "Results executed at " + inputs.getResults().get(0).getExecutedAt());

        playResults(method);
        assertEquals("Number of entries", 35, factory.getWidgetEntries().size());
    }
}
