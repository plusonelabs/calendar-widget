package org.andstatus.todoagenda;

import android.util.Log;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.json.JSONException;

import java.io.IOException;

/**
 * @author yvolk@yurivolkov.com
 */
public class PastDueHeaderWithTasksTest extends BaseWidgetTest {

    @Override
    protected int getNumberOfOpenTasksSources() {
        return 1;
    }

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/205
     */
    public void testPastDueHeaderWithTasks() throws IOException, JSONException {
        final String method = "testPastDueHeaderWithTasks";
        QueryResultsStorage inputs = provider.loadResults(this.getInstrumentation().getContext(),
                org.andstatus.todoagenda.tests.R.raw.past_due_header_with_tasks);
        provider.addResults(inputs.getResults());
        Log.d(method, "Results executed at " + inputs.getResults().get(0).getExecutedAt());

        factory.onDataSetChanged();
        factory.logWidgetEntries(method);
        assertEquals("Past and Due header", DateUtil.DATETIME_MIN, factory.getWidgetEntries().get(0).getStartDate());
        assertEquals("Past Calendar Entry", CalendarEntry.class, factory.getWidgetEntries().get(1).getClass());
        assertEquals("Due task Entry", TaskEntry.class, factory.getWidgetEntries().get(2).getClass());
        assertEquals("Due task Entry", dateTime(2019, 8, 1, 9, 0),
                (factory.getWidgetEntries().get(2)).getStartDate());
        assertEquals("Today header", dateTime(2019, 8, 4),
                (factory.getWidgetEntries().get(3)).getStartDate());
        assertEquals("Future task Entry", TaskEntry.class, factory.getWidgetEntries().get(8).getClass());
        assertEquals("Future task Entry", dateTime(2019, 8, 8, 21, 0),
                (factory.getWidgetEntries().get(8)).getStartDate());
        assertEquals("Number of entries", 9, factory.getWidgetEntries().size());
    }
}
