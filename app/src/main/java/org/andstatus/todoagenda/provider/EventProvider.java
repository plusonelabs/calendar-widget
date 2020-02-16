package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.KeywordsFilter;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.vavr.control.Try;

import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/** Implementation of Empty event provider */
public class EventProvider {

    protected static final String AND_BRACKET = " AND (";
    protected static final String OPEN_BRACKET = "( ";
    protected static final String CLOSING_BRACKET = " )";
    protected static final String AND = " AND ";
    protected static final String OR = " OR ";
    protected static final String EQUALS = " = ";
    protected static final String NOT_EQUALS = " != ";
    protected static final String LTE = " <= ";
    protected static final String IS_NULL = " IS NULL";

    public final EventProviderType type;
    public final Context context;
    public final int widgetId;
    protected final MyContentResolver myContentResolver;

    // Below are parameters, which may change in settings
    protected KeywordsFilter mKeywordsFilter;
    protected DateTime mStartOfTimeRange;
    protected DateTime mEndOfTimeRange;

    public EventProvider(EventProviderType type, Context context, int widgetId) {
        this.type = type;
        this.context = context;
        this.widgetId = widgetId;
        myContentResolver =  new MyContentResolver(type, context, widgetId);
    }

    protected void initialiseParameters() {
        mKeywordsFilter = new KeywordsFilter(getSettings().getHideBasedOnKeywords());
        mStartOfTimeRange = getSettings().getStartOfTimeRange();
        mEndOfTimeRange = getSettings().getEndOfTimeRange();
    }

    @NonNull
    public InstanceSettings getSettings() {
        return myContentResolver.getSettings();
    }

    protected int getAsOpaque(int color) {
        return argb(255, red(color), green(color), blue(color));
    }

    public static Optional<Integer> getColumnIndex(Cursor cursor, String columnName) {
        return Optional.of(cursor.getColumnIndex(columnName))
                .filter(ind -> ind >=0)
                .filter(ind -> cursor.getType(ind) != FIELD_TYPE_NULL);
    }

    public Try<List<EventSource>> fetchAvailableSources() {
        return Try.success(Collections.emptyList());
    }

    protected FilterMode getFilterMode() {
        return getSettings().getFilterMode();
    }
}
