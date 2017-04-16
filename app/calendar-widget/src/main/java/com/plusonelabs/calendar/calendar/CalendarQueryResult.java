package com.plusonelabs.calendar.calendar;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import com.plusonelabs.calendar.DateUtil;
import com.plusonelabs.calendar.prefs.InstanceSettings;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Useful for logging and mocking CalendarContentProvider
 *
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

    private final DateTime executedAt;
    private final int widgetId;
    private Uri uri = Uri.EMPTY;
    private String[] projection = {};
    private String selection = "";
    private String[] selectionArgs = {};
    private String sortOrder = "";
    private final List<CalendarQueryRow> rows = new ArrayList<>();

    public CalendarQueryResult(InstanceSettings settings, Uri uri, String[] projection, String selection,
                               String[] selectionArgs, String sortOrder) {
        this(settings.getWidgetId(), DateUtil.now(settings.getTimeZone()));
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }

    CalendarQueryResult(int widgetId, DateTime executedAt) {
        this.widgetId = widgetId;
        this.executedAt = executedAt;
    }

    public static CalendarQueryResult fromJson(JSONObject json, int widgetId) throws JSONException {
        CalendarQueryResult result = new CalendarQueryResult(widgetId,
                new DateTime(json.getLong(KEY_EXECUTED_AT), dateTimeZoneFromJson(json)));
        result.uri = Uri.parse(json.getString(KEY_URI));
        result.projection = jsonToArrayOfStings(json.getJSONArray(KEY_PROJECTION));
        result.selection = json.getString(KEY_SELECTION);
        result.selectionArgs = jsonToArrayOfStings(json.getJSONArray(KEY_SELECTION_ARGS));
        result.sortOrder = json.getString(KEY_SORT_ORDER);

        JSONArray jsonArray = json.getJSONArray(KEY_ROWS);
        if (jsonArray != null) {
            for (int ind = 0; ind < jsonArray.length(); ind++) {
                result.addRow(CalendarQueryRow.fromJson(jsonArray.getJSONObject(ind)));
            }
        }
        return result;
    }

    static DateTimeZone dateTimeZoneFromJson(JSONObject json) {
        String zoneId = DateUtil.validatedTimeZoneId(json.optString(KEY_TIME_ZONE_ID));
        return DateTimeZone.forID(TextUtils.isEmpty(zoneId) ? "UTC" : zoneId);
    }

    private static String[] jsonToArrayOfStings(JSONArray jsonArray) throws JSONException {
        String[] array = new String[jsonArray != null ? jsonArray.length() : 0];
        if (jsonArray != null) {
            for (int ind = 0; ind < jsonArray.length(); ind++) {
                array[ind] = jsonArray.getString(ind);
            }
        }
        return array;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public DateTime getExecutedAt() {
        return executedAt;
    }

    public Cursor query(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(projection);
        for (CalendarQueryRow row : rows) {
            cursor.addRow(row.getArray(projection));
        }
        return cursor;
    }

    public void addRow(Cursor cursor) {
        addRow(CalendarQueryRow.fromCursor(cursor));
    }

    public void addRow(CalendarQueryRow row) {
        rows.add(row);
    }

    public Uri getUri() {
        return uri;
    }

    public String getSelection() {
        return selection;
    }

    public List<CalendarQueryRow> getRows() {
        return rows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarQueryResult that = (CalendarQueryResult) o;

        if (!uri.equals(that.uri)) return false;
        if (!Arrays.equals(projection, that.projection)) return false;
        if (!selection.equals(that.selection)) return false;
        if (!Arrays.equals(selectionArgs, that.selectionArgs)) return false;
        if (!sortOrder.equals(that.sortOrder)) return false;
        if (rows.size() != that.rows.size()) return false;
        for (int ind = 0; ind < rows.size(); ind++) {
            if (!rows.get(ind).equals(that.rows.get(ind))) {
                return false;
            }
        }
        return true;

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + Arrays.hashCode(projection);
        result = 31 * result + selection.hashCode();
        result = 31 * result + (selectionArgs != null ? Arrays.hashCode(selectionArgs) : 0);
        result = 31 * result + sortOrder.hashCode();
        for (int ind = 0; ind < rows.size(); ind++) {
            result = 31 * result + rows.get(ind).hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        try {
            return toJson().toString(2);
        } catch (JSONException e) {
            return TAG + " Error converting to Json "
                    + e.getMessage() + "; " + rows.toString();
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_EXECUTED_AT, executedAt.getMillis());
        DateTimeZone zone = executedAt.getZone();
        json.put(KEY_TIME_ZONE_ID, zone.getID());
        json.put(KEY_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getOffset(executedAt));
        json.put(KEY_STANDARD_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getStandardOffset(executedAt.getMillis()));
        json.put(KEY_URI, uri != null ? uri.toString() : "");
        json.put(KEY_PROJECTION, arrayOfStingsToJson(projection));
        json.put(KEY_SELECTION, selection != null ? selection : "");
        json.put(KEY_SELECTION_ARGS, arrayOfStingsToJson(selectionArgs));
        json.put(KEY_SORT_ORDER, sortOrder != null ? sortOrder : "");
        JSONArray jsonArray = new JSONArray();
        for (CalendarQueryRow row : rows) {
            jsonArray.put(row.toJson());
        }
        json.put(KEY_ROWS, jsonArray);
        return json;
    }

    private static JSONArray arrayOfStingsToJson(String[] array) {
        JSONArray jsonArray = new JSONArray();
        if (array != null) {
            for (String item : array) {
                jsonArray.put(item);
            }
        }
        return jsonArray;
    }

    public void dropNullColumns() {
        for (CalendarQueryRow row : rows) {
            row.dropNullColumns();
        }
    }
}
