package org.andstatus.todoagenda.provider;

import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import org.andstatus.todoagenda.BaseWidgetTest;
import org.andstatus.todoagenda.calendar.CalendarEventProvider;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Tests of the Testing framework itself
 *
 * @author yvolk@yurivolkov.com
 */
public class MockCalendarContentProviderTest extends BaseWidgetTest {

    private final String[] projection = CalendarEventProvider.getProjection();
    private final String sortOrder = CalendarEventProvider.EVENT_SORT_ORDER;
    private long eventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        QueryResultsStorage.setNeedToStoreResults(true);
    }

    @Override
    protected void tearDown() throws Exception {
        QueryResultsStorage.setNeedToStoreResults(false);
        super.tearDown();
    }

    public void testTestMode() {
        assertTrue("isTestMode should be true", PermissionsUtil.isTestMode());
    }

    public void testTwoEventsToday() {
        QueryResult input1 = addOneResult("");
        QueryResult input2 = addOneResult("SOMETHING=1");

        QueryResult result1 = queryList(input1.getUri(), input1.getSelection());
        assertEquals(1, provider.getQueriesCount());
        assertEquals(input1, result1);
        assertEquals(result1, input1);
        assertEquals(input1, QueryResultsStorage.getStorage().getResults().get(0));

        QueryResult result2 = queryList(input2.getUri(), input2.getSelection());
        assertEquals(2, provider.getQueriesCount());
        assertEquals(input2, result2);
        assertEquals(result2, input2);
        assertEquals(input2, QueryResultsStorage.getStorage().getResults().get(1));

        assertNotSame(result1, result2);

        result1.getRows().get(1).setTitle("Changed title");
        assertNotSame(input1, result1);
        assertNotSame(result1, input1);
    }

    private QueryResult addOneResult(String selection) {
        QueryResult input = new QueryResult(EventProviderType.CALENDAR, provider.getSettings(),
                CalendarContract.Instances.CONTENT_URI, projection, selection, null, sortOrder);
        DateTime today = DateUtil.now(provider.getSettings().getTimeZone()).withTimeAtStartOfDay();
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
        provider.addResult(input);
        return input;
    }

    private QueryResult queryList(Uri uri, String selection) {
        QueryResult result = new QueryResult(EventProviderType.CALENDAR, provider.getSettings(),
                uri, projection, selection, null, sortOrder);
        Cursor cursor = null;
        try {
            cursor = provider.getContext().getContentResolver().query(uri, projection,
                    selection, null, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    if (QueryResultsStorage.getNeedToStoreResults()) {
                        result.addRow(cursor);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        result.dropNullColumns();
        QueryResultsStorage.store(result);
        return result;
    }

    public void testJsonToAndFrom() throws IOException, JSONException {
        QueryResultsStorage inputs1 = provider.loadResults(getInstrumentation().getContext(),
                org.andstatus.todoagenda.tests.R.raw.birthday);
        JSONObject jsonOutput = inputs1.toJson(provider.getContext(), provider.getWidgetId());
        QueryResultsStorage inputs2 =
                QueryResultsStorage.fromJson(provider.getContext(), jsonOutput);
        assertEquals(inputs1, inputs2);
    }
}
