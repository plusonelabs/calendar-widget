package org.andstatus.todoagenda;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class WrongDatesLostEventsTest extends BaseWidgetTest {

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/205
     */
    @Test
    public void testIssue205() throws IOException, JSONException {
        final String method = "testIssue205";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.wrong_dates_lost_events);
        provider.addResults(inputs);

        playResults(method);
        assertEquals("Number of entries", 11, getFactory().getWidgetEntries().size());
        assertEquals("On Saturday", "Maker Fair", ((CalendarEntry) getFactory().getWidgetEntries().get(4)).getEvent().getTitle());
        assertEquals("On Saturday", 6, getFactory().getWidgetEntries().get(4).entryDate.getDayOfWeek());
        assertEquals("On Sunday", "Ribakovs", ((CalendarEntry) getFactory().getWidgetEntries().get(7)).getEvent().getTitle());
        assertEquals("On Sunday", 7, getFactory().getWidgetEntries().get(7).entryDate.getDayOfWeek());
    }
}
