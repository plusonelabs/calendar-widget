package org.andstatus.todoagenda;

import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.LastEntry;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author yvolk@yurivolkov.com
 */
public class BirthdayTest extends BaseWidgetTest {

    @Test
    public void testBirthdayOneDayOnly() throws IOException, JSONException {
        QueryResultsStorage inputs = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.birthday);

        provider.startEditingPreferences();
        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.NONE);
        ApplicationPreferences.setShowPastEventsWithDefaultColor(provider.getContext(), false);
        ApplicationPreferences.setEventRange(provider.getContext(), 30);
        provider.savePreferences();

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
        provider.savePreferences();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0);

        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.TODAY);
        provider.savePreferences();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0);

        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.FOUR_HOURS);
        provider.savePreferences();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 3, 59), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 0);

        ApplicationPreferences.setEventsEnded(provider.getContext(), EndedSomeTimeAgo.YESTERDAY);
        provider.savePreferences();
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 17, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 23, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 0), 0);
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0);

        ApplicationPreferences.setShowPastEventsWithDefaultColor(provider.getContext(), true);
        provider.savePreferences();
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0);
    }

    private void playAtOneTime(QueryResultsStorage inputs, DateTime now, int entriesWithoutLastExpected) {
        inputs.setExecutedAt(now);
        provider.clear();
        provider.addResults(inputs);
        playResults(TAG);
        assertEquals(entriesWithoutLastExpected + 1, getFactory().getWidgetEntries().size());
        if (entriesWithoutLastExpected > 0) {
            CalendarEntry birthday = (CalendarEntry) getFactory().getWidgetEntries().get(1);
            assertEquals(9, birthday.entryDate.dayOfMonth().get());
            assertEquals(0, birthday.entryDate.hourOfDay().get());
            assertEquals(0, birthday.entryDate.minuteOfHour().get());
            assertEquals(0, birthday.entryDate.millisOfDay().get());
            assertEquals(true, birthday.isAllDay());
        }
        LastEntry lastEntry = (LastEntry) getFactory().getWidgetEntries()
                .get(getFactory().getWidgetEntries().size() - 1);
        assertEquals("Last entry: " + lastEntry,
                entriesWithoutLastExpected == 0 ? LastEntry.LastEntryType.EMPTY : LastEntry.LastEntryType.LAST,
                lastEntry.type);

    }
}
