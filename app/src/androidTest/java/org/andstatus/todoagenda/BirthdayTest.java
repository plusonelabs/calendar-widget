package org.andstatus.todoagenda;

import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class BirthdayTest extends BaseWidgetTest {

    @Test
    public void testBirthdayOneDayOnly() throws IOException, JSONException {
        QueryResultsStorage inputs = provider.loadResults(InstrumentationRegistry.getInstrumentation().getContext(),
                org.andstatus.todoagenda.tests.R.raw.birthday);

        provider.startEditing();
        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.NONE);
        ApplicationPreferences.setShowPastEventsWithDefaultColor(provider.getContext(), false);
        ApplicationPreferences.setEventRange(provider.getContext(), 30);
        provider.saveSettings();

        playAtOneTime(inputs, dateTime(2015, 8, 1, 17, 0), 0);
        playAtOneTime(inputs, dateTime(2015, 8, 9, 23, 59), 0);
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 0).plusMillis(1), 2);
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 1), 2);

        playAtOneTime(inputs, dateTime(2015, 9, 8, 17, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 8, 23, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 0, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 11, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 17, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 23, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 0);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 0);

        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.ONE_HOUR);
        provider.saveSettings();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0);

        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.TODAY);
        provider.saveSettings();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0);

        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.FOUR_HOURS);
        provider.saveSettings();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 3, 59), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 0);

        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.YESTERDAY);
        provider.saveSettings();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 17, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 23, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 0), 0);
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0);

        ApplicationPreferences.setShowPastEventsWithDefaultColor(provider.getContext(), true);
        provider.saveSettings();
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0);
    }

    private void playAtOneTime(QueryResultsStorage inputs, DateTime now, int numberOfEntriesExpected) {
        provider.addResults(inputs.getResults());
        DateUtil.setNow(now);
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
        assertEquals(numberOfEntriesExpected, factory.getWidgetEntries().size());
        if (numberOfEntriesExpected > 0) {
            CalendarEntry birthday = (CalendarEntry) factory.getWidgetEntries().get(1);
            assertEquals(9, birthday.getStartDate().dayOfMonth().get());
            assertEquals(0, birthday.getStartDate().hourOfDay().get());
            assertEquals(0, birthday.getStartDate().minuteOfHour().get());
            assertEquals(0, birthday.getStartDate().millisOfDay().get());
            assertEquals(true, birthday.isAllDay());
        }
    }
}
