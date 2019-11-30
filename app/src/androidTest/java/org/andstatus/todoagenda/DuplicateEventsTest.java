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
public class DuplicateEventsTest extends BaseWidgetTest {

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/354
     */
    @Test
    public void testIssue354() throws IOException, JSONException {
        final String method = "testIssue354";
        QueryResultsStorage inputs = provider.loadResults(InstrumentationRegistry.getInstrumentation().getContext(),
                org.andstatus.todoagenda.tests.R.raw.duplicates);
        provider.addResults(inputs.getResults());
        Log.d(method, "Results executed at " + inputs.getResults().get(0).getExecutedAt());

        factory.onDataSetChanged();
        factory.logWidgetEntries(method);
        assertEquals("Number of entries", 40, factory.getWidgetEntries().size());
    }
}
