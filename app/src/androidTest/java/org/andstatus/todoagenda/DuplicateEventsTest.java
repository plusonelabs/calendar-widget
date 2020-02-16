package org.andstatus.todoagenda;

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
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.duplicates);
        provider.addResults(inputs);

        playResults(method);
        assertEquals("Number of entries", 40, getFactory().getWidgetEntries().size());
    }
}
