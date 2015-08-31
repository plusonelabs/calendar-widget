package com.plusonelabs.calendar.calendar;

import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.test.InstrumentationTestCase;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.util.RawResourceUtils;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Tests of the Testing framework itself
 * @author yvolk@yurivolkov.com
 */
public class MockCalendarContentProviderTest extends InstrumentationTestCase {
    private MockCalendarContentProvider mProvider = null;
    private final String[] mProjection = CalendarEventProvider.getProjection();
    private final String mSortOrder = CalendarEventProvider.EVENT_SORT_ORDER;
    private long mEventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mProvider = MockCalendarContentProvider.getContentProvider(this);
        mEventId = 0;
        mProvider.clear();
        CalendarQueryStoredResults.setNeedStoreResults(true);
    }

    public void testTwoEventsToday() {
        CalendarQueryResult input1 = addOneResult("");
        CalendarQueryResult input2 = addOneResult("SOMETHING=1");

        CalendarQueryResult result1 = queryList(input1.getUri(), input1.getSelection());
        assertEquals(1, mProvider.getQueriesCount());
        assertEquals(input1, result1);
        assertEquals(result1, input1);
        assertEquals(input1, CalendarQueryStoredResults.getStored().getResults().get(0));

        CalendarQueryResult result2 = queryList(input2.getUri(), input2.getSelection());
        assertEquals(2, mProvider.getQueriesCount());
        assertEquals(input2, result2);
        assertEquals(result2, input2);
        assertEquals(input2, CalendarQueryStoredResults.getStored().getResults().get(1));

        assertNotSame(result1, result2);

        result1.getRows().get(1).setTitle("Changed title");
        assertNotSame(input1, result1);
        assertNotSame(result1, input1);
    }

    private CalendarQueryResult addOneResult(String selection) {
        CalendarQueryResult input = new CalendarQueryResult(
                CalendarContract.Instances.CONTENT_URI, mProjection, selection, null, mSortOrder);
        DateTime today = DateUtil.now().withTimeAtStartOfDay();
        input.addRow(new CalendarQueryRow().setEventId(++mEventId)
                .setTitle("First Event today").setBegin(today.plusHours(8).getMillis()));
        input.addRow(new CalendarQueryRow()
                        .setEventId(++mEventId)
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
        mProvider.addResult(input);
        return input;
    }

    private CalendarQueryResult queryList(Uri uri, String selection) {
        CalendarQueryResult result = new CalendarQueryResult(uri, mProjection, selection, null, mSortOrder);
        Cursor cursor = null;
        try {
            cursor = mProvider.getContext().getContentResolver().query(uri, mProjection,
                    selection, null, mSortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    if (CalendarQueryStoredResults.getNeedToStoreResults()) {
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
        CalendarQueryStoredResults.store(result);
        return result;
    }

    public void testJsonToAndFrom() throws IOException, JSONException {
        String jsonInputString =
                RawResourceUtils.getString(getInstrumentation().getContext(),
                        com.plusonelabs.calendar.tests.R.raw.birthday);
        CalendarQueryStoredResults inputs1 = CalendarQueryStoredResults.fromJsonString(jsonInputString);
        JSONObject jsonOutput = inputs1.toJson(mProvider.getContext());
        CalendarQueryStoredResults inputs2 = CalendarQueryStoredResults.fromJson(jsonOutput);
        assertEquals(inputs1, inputs2);
    }

    @Override
    protected void tearDown() throws Exception {
        CalendarQueryStoredResults.setNeedStoreResults(false);
        DateUtil.setNow(null);
        mProvider.tearDown();
        super.tearDown();
    }
}
