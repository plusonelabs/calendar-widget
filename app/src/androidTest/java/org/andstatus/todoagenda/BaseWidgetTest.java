package org.andstatus.todoagenda;

import org.andstatus.todoagenda.provider.MockCalendarContentProvider;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

/**
 * @author yvolk@yurivolkov.com
 */
public class BaseWidgetTest {

    final String TAG = this.getClass().getSimpleName();

    protected MockCalendarContentProvider provider = null;
    protected EventRemoteViewsFactory factory = null;

    protected int getNumberOfOpenTasksSources() {
        return 0;
    }

    @Before
    public void setUp() throws Exception {
        provider = MockCalendarContentProvider.getContentProvider(this, getNumberOfOpenTasksSources());
        factory = new EventRemoteViewsFactory(provider.getContext(), provider.getWidgetId());
        assertTrue(factory.getWidgetEntries().isEmpty());
    }

    @After
    public void tearDown() throws Exception {
        provider.tearDown();
    }

    DateTime dateTime(
            int year,
            int monthOfYear,
            int dayOfMonth) {
        return dateTime(year, monthOfYear, dayOfMonth, 0, 0);
    }

    DateTime dateTime(
            int year,
            int monthOfYear,
            int dayOfMonth,
            int hourOfDay,
            int minuteOfHour) {
        return new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0,
                provider.getSettings().getTimeZone());
    }
}
