package org.andstatus.todoagenda;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class ShowOnlyClosestInstanceTest extends BaseWidgetTest {

    @Test
    public void testShowOnlyClosestInstance() throws IOException, JSONException {
        final String method = "testShowOnlyClosestInstance";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.closest_event);
        provider.addResults(inputs);

        playResults(method);

        assertEquals("SnaphotDate", dateTime(2020, 2, 15),
                getSettings().clock().now().withTimeAtStartOfDay());

        List<? extends WidgetEntry> entries = getFactory().getWidgetEntries().stream()
                .filter(e -> e.getTitle().startsWith("Test event 2 that")).collect(Collectors.toList());
        assertEquals("Number of entries of the test event " + entries, 2, entries.size());
        assertNotEquals("Entries should have different IDs\n" + entries + "\n",
                ((CalendarEntry) entries.get(0)).getEvent().getEventId(),
                ((CalendarEntry) entries.get(1)).getEvent().getEventId());
    }
}