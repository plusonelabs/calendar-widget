package org.andstatus.todoagenda.provider;

import android.content.Context;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.KeywordsFilter;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

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

    // Below are parameters, which may change in settings
    protected DateTimeZone zone;
    protected KeywordsFilter mKeywordsFilter;
    protected DateTime mStartOfTimeRange;
    protected DateTime mEndOfTimeRange;

    public EventProvider(EventProviderType type, Context context, int widgetId) {
        this.type = type;
        this.context = context;
        this.widgetId = widgetId;
    }

    protected void initialiseParameters() {
        zone = getSettings().getTimeZone();
        mKeywordsFilter = new KeywordsFilter(getSettings().getHideBasedOnKeywords());
        mStartOfTimeRange = getSettings().getEventsEnded().endedAt(DateUtil.now(zone));
        mEndOfTimeRange = getEndOfTimeRange(DateUtil.now(zone));
    }

    private DateTime getEndOfTimeRange(DateTime now) {
        int dateRange = getSettings().getEventRange();
        return dateRange > 0
                ? now.plusDays(dateRange)
                : now.withTimeAtStartOfDay().plusDays(1);
    }

    @NonNull
    public InstanceSettings getSettings() {
        return AllSettings.instanceFromId(context, widgetId);
    }

    protected int getAsOpaque(int color) {
        return argb(255, red(color), green(color), blue(color));
    }

    public List<EventSource> fetchAvailableSources() {
        return Collections.emptyList();
    }
}
