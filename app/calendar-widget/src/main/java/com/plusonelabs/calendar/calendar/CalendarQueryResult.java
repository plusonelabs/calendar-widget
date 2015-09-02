package com.plusonelabs.calendar.calendar;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.plusonelabs.calendar.DateUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Useful for logging and Mocking CalendarContentProvider
 * @author yvolk@yurivolkov.com
 */
public class CalendarQueryResult {
    private static final String TAG = CalendarQueryResult.class.getSimpleName();
    private static final String KEY_ROWS = "rows";
    private static final String KEY_EXECUTED_AT = "executedAt";
    private static final String KEY_TIME_ZONE_ID = "timeZoneId";
    private static final String KEY_MILLIS_OFFSET_FROM_UTC_TO_LOCAL = "millisOffsetUtcToLocal";
    private static final String KEY_STANDARD_MILLIS_OFFSET_FROM_UTC_TO_LOCAL = "standardMillisOffsetUtcToLocal";
    private static final String KEY_URI = "uri";
    private static final String KEY_PROJECTION = "projection";
    private static final String KEY_SELECTION = "selection";
    private static final String KEY_SELECTION_ARGS = "selectionArgs";
    private static final String KEY_SORT_ORDER = "sortOrder";

    private final DateTime mExecutedAt;
    private Uri mUri = Uri.EMPTY;
    private String[] mProjection = {};
    private String mSelection = "";
    private String[] mSelectionArgs = {};
    private String mSortOrder = "";
    private final List<CalendarQueryRow> mRows = new ArrayList<>();

    public CalendarQueryResult (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        mExecutedAt = DateUtil.now();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    CalendarQueryResult(DateTime executedAt) {
        mExecutedAt = executedAt;
    }

    public static CalendarQueryResult fromJson(JSONObject jso) throws JSONException {
        CalendarQueryResult result = new CalendarQueryResult(new DateTime(jso.getLong(KEY_EXECUTED_AT)));
        result.mUri = Uri.parse(jso.getString(KEY_URI));
        result.mProjection = jsonToArrayOfStings(jso.getJSONArray(KEY_PROJECTION));
        result.mSelection = jso.getString(KEY_SELECTION);
        result.mSelectionArgs = jsonToArrayOfStings(jso.getJSONArray(KEY_SELECTION_ARGS));
        result.mSortOrder = jso.getString(KEY_SORT_ORDER);

        JSONArray jsa = jso.getJSONArray(KEY_ROWS);
        if (jsa != null) {
            for (int ind=0; ind < jsa.length(); ind++) {
                result.addRow(CalendarQueryRow.fromJson(jsa.getJSONObject(ind)));
            }
        }
        return result;
    }

    private static String[] jsonToArrayOfStings(JSONArray jsa) throws JSONException {
        String[] array = new String[ jsa != null ? jsa.length() : 0];
        if (jsa != null) {
            for (int ind=0; ind < jsa.length(); ind++) {
                array[ind] = jsa.getString(ind);
            }
        }
        return array;
    }

    public Cursor query(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(projection);
        for (CalendarQueryRow row : mRows) {
            cursor.addRow(row.getArray(projection));
        }
        return cursor;
    }

    public void addRow(Cursor cursor) {
        addRow(CalendarQueryRow.fromCursor(cursor));
    }

    public void addRow(CalendarQueryRow row) {
        mRows.add(row);
    }

    public Uri getUri() {
        return mUri;
    }

    public String getSelection() {
        return mSelection;
    }

    public List<CalendarQueryRow> getRows() {
        return mRows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarQueryResult that = (CalendarQueryResult) o;

        if (!mUri.equals(that.mUri)) return false;
        if (!Arrays.equals(mProjection, that.mProjection)) return false;
        if (!mSelection.equals(that.mSelection)) return false;
        if (!Arrays.equals(mSelectionArgs, that.mSelectionArgs)) return false;
        if (!mSortOrder.equals(that.mSortOrder)) return false;
        if (mRows.size() != that.mRows.size()) return false;
        for (int ind=0; ind < mRows.size(); ind++) {
            if (!mRows.get(ind).equals(that.mRows.get(ind))) {
                return false;
            }
        }
        return true;

    }

    @Override
    public int hashCode() {
        int result = mUri.hashCode();
        result = 31 * result + Arrays.hashCode(mProjection);
        result = 31 * result + mSelection.hashCode();
        result = 31 * result + (mSelectionArgs != null ? Arrays.hashCode(mSelectionArgs) : 0);
        result = 31 * result + mSortOrder.hashCode();
        for (int ind=0; ind < mRows.size(); ind++) {
            result = 31 * result + mRows.get(ind).hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        try {
            return toJson().toString(2);
        } catch (JSONException e) {
            return TAG + " Error converting to Json "
                    + e.getMessage() + "; " + mRows.toString();
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jso = new JSONObject();
        jso.put(KEY_EXECUTED_AT, mExecutedAt.getMillis());
        DateTimeZone zone = mExecutedAt.getZone();
        jso.put(KEY_TIME_ZONE_ID, zone.getID());
        jso.put(KEY_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getOffset(mExecutedAt));
        jso.put(KEY_STANDARD_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getStandardOffset(mExecutedAt.getMillis()));
        jso.put(KEY_URI, mUri != null ? mUri.toString() : "");
        jso.put(KEY_PROJECTION, arrayOfStingsToJson(mProjection));
        jso.put(KEY_SELECTION, mSelection != null ? mSelection : "");
        jso.put(KEY_SELECTION_ARGS, arrayOfStingsToJson(mSelectionArgs));
        jso.put(KEY_SORT_ORDER, mSortOrder != null ? mSortOrder : "");
        JSONArray jsa = new JSONArray();
        for (CalendarQueryRow row : mRows) {
            jsa.put(row.toJson());
        }
        jso.put(KEY_ROWS, jsa);
        return jso;
    }

    private static JSONArray arrayOfStingsToJson(String[] array) {
        JSONArray jsa = new JSONArray();
        if (array != null) {
            for (String item : array) {
                jsa.put(item);
            }
        }
        return jsa;
    }

    public void dropNullColumns() {
        for (CalendarQueryRow row : mRows) {
            row.dropNullColumns();
        }
    }
}
