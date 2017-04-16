package com.plusonelabs.calendar.calendar;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.provider.CalendarContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Useful for logging and Mocking CalendarContentProvider
 *
 * @author yvolk@yurivolkov.com
 */
public class CalendarQueryRow {

    private static class TypedValue {

        private static final String KEY_TYPE = "type";
        private static final String KEY_VALUE = "value";

        final CursorFieldType type;
        final Object value;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypedValue that = (TypedValue) o;

            if (type != CursorFieldType.UNKNOWN && that.type != CursorFieldType.UNKNOWN) {
                if (type != that.type) return false;
            }
            return !(value != null ? !value.toString().equals(that.value.toString()) : that.value != null);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + (value != null ? value.toString().hashCode() : 0);
            return result;
        }

        private enum CursorFieldType {
            UNKNOWN(-1),
            STRING(Cursor.FIELD_TYPE_STRING) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getString(columnIndex);
                }
            },
            INTEGER(Cursor.FIELD_TYPE_INTEGER) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getLong(columnIndex);
                }
            },
            BLOB(Cursor.FIELD_TYPE_BLOB) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getBlob(columnIndex);
                }
            },
            FLOAT(Cursor.FIELD_TYPE_FLOAT) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getDouble(columnIndex);
                }
            },
            NULL(Cursor.FIELD_TYPE_NULL);

            final int code;

            CursorFieldType(int fieldType) {
                code = fieldType;
            }

            public Object columnToObject(Cursor cursor, int columnIndex) {
                return null;
            }

            public static CursorFieldType fromColumnType(int cursorColumnType) {
                for (CursorFieldType val : values()) {
                    if (val.code == cursorColumnType) {
                        return val;
                    }
                }
                return UNKNOWN;
            }

        }

        public static TypedValue fromJson(JSONObject json) {
            CursorFieldType type = CursorFieldType.UNKNOWN;
            if (json.has(KEY_TYPE)) {
                type = CursorFieldType.fromColumnType(json.optInt(KEY_TYPE));
            }
            return new TypedValue(type, json.opt(KEY_VALUE));
        }

        public TypedValue(Cursor cursor, int columnIndex) {
            type = CursorFieldType.fromColumnType(cursor.getType(columnIndex));
            value = type.columnToObject(cursor, columnIndex);
        }

        public TypedValue(Object object) {
            this(CursorFieldType.UNKNOWN, object);
        }

        public TypedValue(CursorFieldType type, Object object) {
            this.type = type;
            value = object;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(KEY_TYPE, type.code);
            json.put(KEY_VALUE, value);
            return json;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarQueryRow that = (CalendarQueryRow) o;
        if (mRow.size() != that.mRow.size()) {
            return false;
        }
        for (Map.Entry<String, TypedValue> entry : mRow.entrySet()) {
            if (!that.mRow.containsKey(entry.getKey())) {
                return false;
            }
            if (!entry.getValue().equals(that.mRow.get(entry.getKey()))) {
                return false;
            }
        }
        return mRow.equals(that.mRow);

    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Map.Entry<String, TypedValue> entry : mRow.entrySet()) {
            result += 31 * entry.getValue().hashCode();
        }
        return result;
    }

    private final Map<String, TypedValue> mRow = new HashMap<>();

    private CalendarQueryRow setColumn(String columnName, Object columnValue) {
        mRow.put(columnName, new TypedValue(columnValue));
        return this;
    }

    public CalendarQueryRow setEventId(Object obj) {
        return setColumn(CalendarContract.Instances.EVENT_ID, obj);
    }

    public CalendarQueryRow setTitle(Object obj) {
        return setColumn(CalendarContract.Instances.TITLE, obj);
    }

    public CalendarQueryRow setBegin(Object obj) {
        return setColumn(CalendarContract.Instances.BEGIN, obj);
    }

    public CalendarQueryRow setEnd(Object obj) {
        return setColumn(CalendarContract.Instances.END, obj);
    }

    public CalendarQueryRow setAllDay(Object obj) {
        return setColumn(CalendarContract.Instances.ALL_DAY, obj);
    }

    public CalendarQueryRow setEventLocation(Object obj) {
        return setColumn(CalendarContract.Instances.EVENT_LOCATION, obj);
    }

    public CalendarQueryRow setHasAlarm(Object obj) {
        return setColumn(CalendarContract.Instances.HAS_ALARM, obj);
    }

    public CalendarQueryRow setRRule(Object obj) {
        return setColumn(CalendarContract.Instances.RRULE, obj);
    }

    @Override
    public String toString() {
        try {
            return toJson().toString(2);
        } catch (JSONException e) {
            return this.getClass().getSimpleName() + "Error converting to Json "
                    + e.getMessage() + "; " + mRow.toString();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public CalendarQueryRow setDisplayColor(Object obj) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return setColumn(CalendarContract.Instances.DISPLAY_COLOR, obj);
        } else {
            return setColumn(CalendarContract.Instances.EVENT_COLOR, obj);
        }
    }

    public Object[] getArray(String[] projection) {
        Object[] values = new Object[projection.length];
        for (int ind = 0; ind < projection.length; ind++) {
            values[ind] = get(projection[ind]);
        }
        return values;
    }

    private Object get(String columnName) {
        if (mRow.containsKey(columnName)) {
            return mRow.get(columnName).value;
        }
        return null;
    }

    public static CalendarQueryRow fromCursor(Cursor cursor) {
        CalendarQueryRow row = new CalendarQueryRow();
        if (cursor != null && !cursor.isClosed()) {
            for (int ind = 0; ind < cursor.getColumnCount(); ind++) {
                row.mRow.put(cursor.getColumnName(ind), new TypedValue(cursor, ind));
            }
        }
        return row;
    }

    public static CalendarQueryRow fromJson(JSONObject json) throws JSONException {
        CalendarQueryRow row = new CalendarQueryRow();
        if (json != null) {
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                String columnName = it.next();
                row.mRow.put(columnName, TypedValue.fromJson(json.getJSONObject(columnName)));
            }
        }
        return row;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, TypedValue> entry : mRow.entrySet()) {
            json.put(entry.getKey(), entry.getValue().toJson());
        }
        return json;
    }

    public void dropNullColumns() {
        for (Iterator<Map.Entry<String, TypedValue>> it = mRow.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, TypedValue> entry = it.next();
            if (entry.getValue().type == TypedValue.CursorFieldType.NULL
                    || entry.getValue().value == null) {
                it.remove();
            }
        }
    }

}
