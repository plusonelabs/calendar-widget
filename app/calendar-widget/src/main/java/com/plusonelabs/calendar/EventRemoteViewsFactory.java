package com.plusonelabs.calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.plusonelabs.calendar.calendar.CalendarEventVisualizer;
import com.plusonelabs.calendar.prefs.InstanceSettings;
import com.plusonelabs.calendar.widget.DayHeader;
import com.plusonelabs.calendar.widget.WidgetEntry;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.plusonelabs.calendar.CalendarIntentUtil.*;
import static com.plusonelabs.calendar.RemoteViewsUtil.*;
import static com.plusonelabs.calendar.Theme.themeNameToResId;

public class EventRemoteViewsFactory implements RemoteViewsFactory {

    private final Context context;
    private final int widgetId;
    private volatile List<WidgetEntry> mWidgetEntries = new ArrayList<>();
    private final List<IEventVisualizer<?>> eventProviders;

    public EventRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        eventProviders = new ArrayList<>();
        eventProviders.add(new CalendarEventVisualizer(context, widgetId));
    }

    public void onCreate() {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(getSettings()));
    }

    public void onDestroy() {
        // Empty
    }

    public int getCount() {
        return mWidgetEntries.size();
    }

    public RemoteViews getViewAt(int position) {
        List<WidgetEntry> widgetEntries = mWidgetEntries;
        if (position < widgetEntries.size()) {
            WidgetEntry entry = widgetEntries.get(position);
            if (entry instanceof DayHeader) {
                return getRemoteView((DayHeader) entry);
            }
            for (IEventVisualizer<?> eventProvider : eventProviders) {
                if (entry.getClass().isAssignableFrom(eventProvider.getSupportedEventEntryType())) {
                    return eventProvider.getRemoteView(entry);
                }
            }
        }
        return null;
    }

    private RemoteViews getRemoteView(DayHeader dayHeader) {
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

    @NonNull
    private InstanceSettings getSettings() {
        return InstanceSettings.fromId(context, widgetId);
    }

    public void onDataSetChanged() {
        context.setTheme(themeNameToResId(getSettings().getEntryTheme()));
        if (getSettings().getShowDayHeaders())
            mWidgetEntries = addDayHeaders(getEventEntries());
        else
            mWidgetEntries = getEventEntries();
    }

    private List<WidgetEntry> getEventEntries() {
        List<WidgetEntry> entries = new ArrayList<>();
        for (IEventVisualizer<?> eventProvider : eventProviders) {
            entries.addAll(eventProvider.getEventEntries());
        }
        return entries;
    }

    private List<WidgetEntry> addDayHeaders(List<WidgetEntry> listIn) {
        List<WidgetEntry> listOut = new ArrayList<>();
        if (!listIn.isEmpty()) {
            boolean showDaysWithoutEvents = getSettings().getShowDaysWithoutEvents();
            DayHeader curDayBucket = new DayHeader(new DateTime(0, getSettings().getTimeZone()));
            for (WidgetEntry entry : listIn) {
                DateTime nextStartOfDay = entry.getStartDay();
                if (!nextStartOfDay.isEqual(curDayBucket.getStartDay())) {
                    if (showDaysWithoutEvents) {
                        addEmptyDayHeadersBetweenTwoDays(listOut, curDayBucket.getStartDay(), nextStartOfDay);
                    }
                    curDayBucket = new DayHeader(nextStartOfDay);
                    listOut.add(curDayBucket);
                }
                listOut.add(entry);
            }
        }
        return listOut;
    }

    public void logWidgetEntries(String tag) {
        for (int ind = 0; ind < getWidgetEntries().size(); ind++) {
            WidgetEntry widgetEntry = getWidgetEntries().get(ind);
            Log.v(tag, String.format("%02d ", ind) + widgetEntry.toString());
        }
    }

    List<WidgetEntry> getWidgetEntries() {
        return mWidgetEntries;
    }

    private void addEmptyDayHeadersBetweenTwoDays(List<WidgetEntry> entries, DateTime fromDayExclusive, DateTime toDayExclusive) {
        DateTime emptyDay = fromDayExclusive.plusDays(1);
        DateTime today = DateUtil.now(getSettings().getTimeZone()).withTimeAtStartOfDay();
        if (emptyDay.isBefore(today)) {
            emptyDay = today;
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            entries.add(new DayHeader(emptyDay));
            emptyDay = emptyDay.plusDays(1);
        }
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        int result = 3; // we have 3 because of the "left", "right" and "center" day headers
        for (IEventVisualizer<?> eventProvider : eventProviders) {
            result += eventProvider.getViewTypeCount();
        }
        return result;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

}
