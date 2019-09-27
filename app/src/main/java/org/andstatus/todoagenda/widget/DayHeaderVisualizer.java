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
import org.andstatus.todoagenda.util.DateUtil;

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

    public DayHeaderVisualizer(Context context, int widgetId) {
        super(new EventProvider(EventProviderType.EMPTY, context, widgetId));
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        if(!(eventEntry instanceof DayHeader)) return null;

        DayHeader entry = (DayHeader) eventEntry;
        String alignment = getSettings().getDayHeaderAlignment();
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), Alignment.valueOf(alignment).getLayoutId());
        String dateString = (entry.getStartDate().equals(DateUtil.DATETIME_MIN)
                ? getContext().getString(R.string.past_header)
                : DateUtil.createDayHeaderTitle(getSettings(), entry.getStartDate()))
            .toUpperCase(Locale.getDefault());
        rv.setTextViewText(R.id.day_header_title, dateString);
        setTextSize(getSettings(), rv, R.id.day_header_title, R.dimen.day_header_title);
        setBackgroundColor(rv, R.id.day_header, getSettings().getEntryBackgroundColor(entry));
        ContextThemeWrapper shadingContext = getSettings().getShadingContext(TextShadingPref.getDayHeader(entry));
        setTextColorFromAttr(shadingContext, rv, R.id.day_header_title, R.attr.dayHeaderTitle);
        if (position == 0) {
            rv.setViewVisibility(R.id.day_header_separator, View.GONE);
        } else {
            rv.setViewVisibility(R.id.day_header_separator, View.VISIBLE);
            setBackgroundColorFromAttr(shadingContext, rv, R.id.day_header_separator, R.attr.dayHeaderSeparator);
        }
        setPadding(getSettings(), rv, R.id.day_header_title,
                R.dimen.day_header_padding_left,
                position == 0 ? R.dimen.day_header_padding_top_first : R.dimen.day_header_padding_top,
                R.dimen.day_header_padding_right, R.dimen.day_header_padding_bottom);
        Intent intent = createOpenCalendarAtDayIntent(entry.getStartDate());
        rv.setOnClickFillInIntent(R.id.day_header, intent);
        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 3; // we have 3 because of the "left", "right" and "center" day headers
    }

    @Override
    public List<DayHeader> queryEventEntries() {
        return Collections.emptyList();
    }
}
