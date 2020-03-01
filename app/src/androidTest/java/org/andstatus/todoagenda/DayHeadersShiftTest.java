package org.andstatus.todoagenda;

import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.widget.DayHeader;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class DayHeadersShiftTest extends BaseWidgetTest {

    @Test
    public void testDayHeadersShift() throws IOException, JSONException {
        final String method = "testDayHeadersShift";
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.day_headers_shift);
        provider.addResults(inputs);

        playResults(method);
        DayHeader dayHeader0 = (DayHeader) getFactory().getWidgetEntries().get(0);


        assertEquals("First day header should be Jan 8\n" + getFactory().getWidgetEntries(), 8,
                dayHeader0.entryDate.dayOfMonth().get());
        CharSequence dayHeaderTitle = getSettings().dayHeaderDateFormatter().formatDate(dayHeader0.entryDate);
        assertEquals("First day header should show Jan 8\n" + getFactory().getWidgetEntries() + "\n",
                "Wednesday, January 8", dayHeaderTitle);
    }
}
