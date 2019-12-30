package org.andstatus.todoagenda;

import android.util.Log;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class TodaysColorsEventsTest extends BaseWidgetTest {

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/327
     */
    @Test
    public void testIssue327() throws IOException, JSONException {
        final String method = "testIssue327";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(InstrumentationRegistry.getInstrumentation().getContext(),
                org.andstatus.todoagenda.tests.R.raw.todays_colors);
        provider.addResults(inputs.getResults());
        Log.d(method, "Results executed at " + inputs.getResults().get(0).getExecutedAt());

        playResults(method);
        assertEquals("Number of entries", 43, factory.getWidgetEntries().size());
    }
}
