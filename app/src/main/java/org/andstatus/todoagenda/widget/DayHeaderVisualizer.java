package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.Alignment;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.provider.EventProviderType;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setPadding;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

public class DayHeaderVisualizer extends WidgetEntryVisualizer<DayHeader> {

    private final Alignment alignment;
    private final boolean horizontalLineBelowDayHeader;

    public DayHeaderVisualizer(Context context, int widgetId) {
        super(new EventProvider(EventProviderType.EMPTY, context, widgetId));
        alignment = Alignment.valueOf(getSettings().getDayHeaderAlignment());
        horizontalLineBelowDayHeader = getSettings().getHorizontalLineBelowDayHeader();
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        if(!(eventEntry instanceof DayHeader)) return null;

        DayHeader entry = (DayHeader) eventEntry;
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), horizontalLineBelowDayHeader
                ? R.layout.day_header_separator_below : R.layout.day_header_separator_above);
        rv.setInt(R.id.day_header_title_wrapper, "setGravity", alignment.gravity);

        ContextThemeWrapper shadingContext = getSettings().getShadingContext(TextShadingPref.forDayHeader(entry));
        setBackgroundColor(rv, R.id.day_header, getSettings().getEntryBackgroundColor(entry));
        setDayHeaderTitle(position, entry, rv, shadingContext);
        setDayHeaderSeparator(position, rv, shadingContext);
        Intent intent = createOpenCalendarAtDayIntent(entry.entryDate);
        rv.setOnClickFillInIntent(R.id.day_header, intent);
        return rv;
    }

    private void setDayHeaderTitle(int position, DayHeader entry, RemoteViews rv, ContextThemeWrapper shadingContext) {
        String dateString = getTitleString(entry).toString().toUpperCase(Locale.getDefault());
        rv.setTextViewText(R.id.day_header_title, dateString);
        setTextSize(getSettings(), rv, R.id.day_header_title, R.dimen.day_header_title);
        setTextColorFromAttr(shadingContext, rv, R.id.day_header_title, R.attr.dayHeaderTitle);

        int paddingTopId = horizontalLineBelowDayHeader
                ? R.dimen.day_header_padding_bottom
                : (position == 0 ? R.dimen.day_header_padding_top_first : R.dimen.day_header_padding_top);
        int paddingBottomId = horizontalLineBelowDayHeader
                ? R.dimen.day_header_padding_top
                : R.dimen.day_header_padding_bottom;
        setPadding(getSettings(), rv, R.id.day_header_title,
                R.dimen.day_header_padding_left, paddingTopId, R.dimen.day_header_padding_right, paddingBottomId);
    }

    protected CharSequence getTitleString(DayHeader entry) {
        switch (entry.entryPosition) {
            case PAST_AND_DUE_HEADER:
                return getContext().getString(R.string.past_header);
            case END_OF_LIST_HEADER:
                return getContext().getString(R.string.end_of_list_header);
            default:
                return getSettings().dayHeaderDateFormatter().formatDate(entry.entryDate);
        }
    }

    private void setDayHeaderSeparator(int position, RemoteViews rv, ContextThemeWrapper shadingContext) {
        int viewId = R.id.day_header_separator;
        if (horizontalLineBelowDayHeader) {
            setBackgroundColorFromAttr(shadingContext, rv, viewId, R.attr.dayHeaderSeparator);
        } else {
            if (position == 0) {
                rv.setViewVisibility(viewId, View.GONE);
            } else {
                rv.setViewVisibility(viewId, View.VISIBLE);
                setBackgroundColorFromAttr(shadingContext, rv, viewId, R.attr.dayHeaderSeparator);
            }
        }
    }

    @Override
    public List<DayHeader> queryEventEntries() {
        return Collections.emptyList();
    }
}
