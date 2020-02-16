package org.andstatus.todoagenda.provider;

import android.net.Uri;
import android.provider.CalendarContract;

import org.andstatus.todoagenda.BaseWidgetTest;
import org.andstatus.todoagenda.calendar.CalendarEventProvider;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests of the Testing framework itself
 *
 * @author yvolk@yurivolkov.com
 */
public class MockCalendarContentProviderTest extends BaseWidgetTest {
    private final static String TAG = MockCalendarContentProviderTest.class.getSimpleName();

    private final String[] projection = CalendarEventProvider.getProjection();
    private final String sortOrder = CalendarEventProvider.EVENT_SORT_ORDER;
    private long eventId = 0;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        QueryResultsStorage.setNeedToStoreResults(false, 0);
        super.tearDown();
    }

    @Test
    public void testTestMode() {
        assertTrue("isTestMode should be true", PermissionsUtil.isTestMode());
    }

    @Test
    public void testTwoEventsToday() {
        QueryResultsStorage results = new QueryResultsStorage();
        QueryResult input1 = newResult("");
        results.addResult(input1);
        QueryResult input2 = newResult("SOMETHING=1");
        results.addResult(input2);
        provider.addResults(results);
        provider.updateAppSettings(TAG);

        QueryResultsStorage.setNeedToStoreResults(true, provider.getWidgetId());
        MyContentResolver resolver = new MyContentResolver(EventProviderType.CALENDAR, provider.getContext(), provider.getWidgetId());

        QueryResult result1 = queryList(resolver, input1.getUri(), input1.getSelection());
        List<QueryResult> stored1 = QueryResultsStorage.getStorage().getResults(EventProviderType.CALENDAR, provider.getWidgetId());

        assertEquals(input1, result1);
        assertEquals(result1, input1);
        assertEquals("Results 1 size\n" + stored1, 1, stored1.size());
        assertEquals(input1, stored1.get(0));

        QueryResult result2 = queryList(resolver, input2.getUri(), input2.getSelection());
        List<QueryResult> stored2 = QueryResultsStorage.getStorage().getResults(EventProviderType.CALENDAR, provider.getWidgetId());

        assertEquals(input2, result2);
        assertEquals(result2, input2);
        assertEquals("Results 2 size\n" + stored2, 2, stored2.size());
        assertEquals(input2, stored2.get(1));

        assertNotSame(result1, result2);

        result1.getRows().get(1).setTitle("Changed title");
        assertNotSame(input1, result1);
        assertNotSame(result1, input1);
    }

    private QueryResult newResult(String selection) {
        QueryResult input = new QueryResult(EventProviderType.CALENDAR, getSettings(),
                CalendarContract.Instances.CONTENT_URI, projection, selection, null, sortOrder);
        DateTime today = getSettings().clock().now().withTimeAtStartOfDay();
        input.addRow(new QueryRow().setEventId(++eventId)
                .setTitle("First Event today").setBegin(today.plusHours(8).getMillis()));
        input.addRow(new QueryRow()
                .setEventId(++eventId)
                .setTitle("Event with all known attributes")
                .setBegin(today.plusHours(12).getMillis())
                .setEnd(today.plusHours(13).getMillis())
                .setDisplayColor(0xFF00FF)
                .setAllDay(false)
                .setEventLocation("somewhere")
                .setHasAlarm(true)
                .setRRule("what's this?")
        );
        assertEquals(CalendarContract.Instances.CONTENT_URI, input.getUri());
        assertEquals(selection, input.getSelection());
        return input;
    }

    private QueryResult queryList(MyContentResolver resolver, Uri uri, String selection) {
        QueryResult result = new QueryResult(EventProviderType.CALENDAR, getSettings(),
                uri, projection, selection, null, sortOrder);

        result = resolver.foldEvents(uri, projection, selection, null, sortOrder, result, r -> cursor -> {
            r.addRow(cursor);
            return r;
        });
        result.dropNullColumns();
        return result;
    }

    @Test
    public void testJsonToAndFrom() throws IOException, JSONException {
        QueryResultsStorage inputs1 = provider.loadResultsAndSettings(
                org.andstatus.todoagenda.tests.R.raw.birthday);
        JSONObject jsonOutput = inputs1.toJson(provider.getContext(), provider.getWidgetId(), true);
        QueryResultsStorage inputs2 = QueryResultsStorage.fromJson(provider.getWidgetId(), jsonOutput);
        assertEquals(inputs1, inputs2);
    }
}
