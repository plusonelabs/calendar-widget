package org.andstatus.todoagenda;

import android.test.InstrumentationTestCase;

import org.andstatus.todoagenda.provider.MockCalendarContentProvider;
import org.joda.time.DateTime;

/**
 * @author yvolk@yurivolkov.com
 */
public class BaseWidgetTest extends InstrumentationTestCase {

    final String TAG = this.getClass().getSimpleName();

    protected MockCalendarContentProvider provider = null;
    protected EventRemoteViewsFactory factory = null;

    protected int getNumberOfOpenTasksSources() {
        return 0;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = MockCalendarContentProvider.getContentProvider(this, getNumberOfOpenTasksSources());
        factory = new EventRemoteViewsFactory(provider.getContext(), provider.getWidgetId());
        assertTrue(factory.getWidgetEntries().isEmpty());
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
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
