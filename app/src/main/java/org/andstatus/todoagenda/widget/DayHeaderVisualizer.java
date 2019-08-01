package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.Alignment;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
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
    public final Context context;
    public final int widgetId;


    public DayHeaderVisualizer(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
    }

    @Override
    public RemoteViews getRemoteView(WidgetEntry eventEntry) {
        if(!(eventEntry instanceof DayHeader)) return null;

        DayHeader dayHeader = (DayHeader) eventEntry;
        String alignment = getSettings().getDayHeaderAlignment();
        RemoteViews rv = new RemoteViews(context.getPackageName(), Alignment.valueOf(alignment).getLayoutId());
        String dateString = DateUtil.createDayHeaderTitle(getSettings(), dayHeader.getStartDate())
                .toUpperCase(Locale.getDefault());
        rv.setTextViewText(R.id.day_header_title, dateString);
        setTextSize(getSettings(), rv, R.id.day_header_title, R.dimen.day_header_title);
        setTextColorFromAttr(context, rv, R.id.day_header_title, R.attr.dayHeaderTitle);
        setBackgroundColor(rv, R.id.day_header,
                dayHeader.getStartDay().plusDays(1).isBefore(DateUtil.now(getSettings().getTimeZone())) ?
                        getSettings().getPastEventsBackgroundColor() : Color.TRANSPARENT);
        setBackgroundColorFromAttr(context, rv, R.id.day_header_separator, R.attr.dayHeaderSeparator);
        setPadding(getSettings(), rv, R.id.day_header_title, 0, R.dimen.day_header_padding_top,
                R.dimen.day_header_padding_right, R.dimen.day_header_padding_bottom);
        Intent intent = createOpenCalendarAtDayIntent(dayHeader.getStartDate());
        rv.setOnClickFillInIntent(R.id.day_header, intent);
        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 3; // we have 3 because of the "left", "right" and "center" day headers
    }

    @Override
    public List<DayHeader> getEventEntries() {
        return Collections.emptyList();
    }

    private InstanceSettings getSettings() {
        return InstanceSettings.fromId(context, widgetId);
    }
}
