package com.plusonelabs.calendar;

import android.test.InstrumentationTestCase;

import com.plusonelabs.calendar.calendar.CalendarQueryRow;
import com.plusonelabs.calendar.calendar.MockCalendarContentProvider;
import com.plusonelabs.calendar.prefs.ApplicationPreferences;
import com.plusonelabs.calendar.widget.CalendarEntry;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

/**
 * @author yvolk@yurivolkov.com
 */
public class RecurringEventsTest extends InstrumentationTestCase {

    private static final String TAG = RecurringEventsTest.class.getSimpleName();

    private MockCalendarContentProvider provider = null;
    private EventRemoteViewsFactory factory = null;
    private int eventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = MockCalendarContentProvider.getContentProvider(this);
        factory = new EventRemoteViewsFactory(provider.getContext(), provider.getWidgetId());
        assertTrue(factory.getWidgetEntries().isEmpty());
        eventId = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
    }

    /**
     * @see <a href="https://github.com/plusonelabs/calendar-widget/issues/191">Issue 191</a> and
     * <a href="https://github.com/plusonelabs/calendar-widget/issues/46">Issue 46</a>
     */
    public void testShowRecurringEvents() {
        generateEventInstances();
        assertEquals("Entries: " + factory.getWidgetEntries().size(), 15, countCalendarEntries());
        provider.startEditing();
        ApplicationPreferences.setShowOnlyClosestInstanceOfRecurringEvent(provider.getContext(), true);
        provider.saveSettings();
        generateEventInstances();
        assertEquals("Entries: " + factory.getWidgetEntries().size(), 1, countCalendarEntries());
    }

    int countCalendarEntries() {
        int count = 0;
        for (WidgetEntry widgetEntry : factory.getWidgetEntries()) {
            if (CalendarEntry.class.isAssignableFrom(widgetEntry.getClass())) {
                count++;
            }
        }
        return count;
    }

    void generateEventInstances() {
        provider.clear();
        DateTime date = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
        long millis = date.getMillis() + TimeUnit.HOURS.toMillis(10);
        eventId++;
        for (int ind = 0; ind < 15; ind++) {
            millis += TimeUnit.DAYS.toMillis(1);
            provider.addRow(new CalendarQueryRow().setEventId(eventId).setTitle("Work each day")
                    .setBegin(millis).setEnd(millis + TimeUnit.HOURS.toMillis(9)));
        }
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
    }
}
