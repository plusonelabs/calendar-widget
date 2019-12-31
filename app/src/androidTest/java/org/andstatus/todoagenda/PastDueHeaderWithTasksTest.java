package org.andstatus.todoagenda;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.util.MyClock;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.LastEntry;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class PastDueHeaderWithTasksTest extends BaseWidgetTest {

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/205
     */
    @Test
    public void testPastDueHeaderWithTasks() throws IOException, JSONException {
        final String method = "testPastDueHeaderWithTasks";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.past_due_header_with_tasks);
        provider.addResults(inputs.getResults());

        playResults(method);
        assertEquals("Past and Due header", MyClock.DATETIME_MIN, factory.getWidgetEntries().get(0).entryDate);
        assertEquals("Past Calendar Entry", CalendarEntry.class, factory.getWidgetEntries().get(1).getClass());
        assertEquals("Due task Entry", TaskEntry.class, factory.getWidgetEntries().get(2).getClass());
        assertEquals("Due task Entry", dateTime(2019, 8, 1, 9, 0),
                (factory.getWidgetEntries().get(2)).entryDate);
        assertEquals("Today header", dateTime(2019, 8, 4),
                (factory.getWidgetEntries().get(3)).entryDate);
        assertEquals("Future task Entry", TaskEntry.class, factory.getWidgetEntries().get(8).getClass());
        assertEquals("Future task Entry", dateTime(2019, 8, 8, 21, 0),
                (factory.getWidgetEntries().get(8)).entryDate);
        assertEquals("Last Entry", LastEntry.LastEntryType.LAST, ((LastEntry) factory.getWidgetEntries().get(9)).type);
        assertEquals("Number of entries", 10, factory.getWidgetEntries().size());
    }
}
