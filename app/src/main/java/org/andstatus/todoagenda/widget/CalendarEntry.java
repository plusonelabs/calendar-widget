package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.text.TextUtils;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;

import static org.andstatus.todoagenda.util.MyClock.isDateDefined;

public class CalendarEntry extends WidgetEntry<CalendarEntry> {

    private static final String ARROW = "→";
    private static final String SPACE = " ";
    static final String SPACE_DASH_SPACE = " - ";

    private boolean allDay;
    private CalendarEvent event;

    public static CalendarEntry fromEvent(InstanceSettings settings, CalendarEvent event, DateTime entryDate) {
        CalendarEntry entry = new CalendarEntry(settings, entryDate, event.getEndDate());
        entry.allDay = event.isAllDay();
        entry.event = event;
        return entry;
    }

    private CalendarEntry(InstanceSettings settings, DateTime entryDate, DateTime endDate) {
        super(settings, WidgetEntry.getEntryPosition(settings, entryDate, endDate), entryDate, endDate);
    }

    @Override
    public String getTitle() {
        String title = event.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = getContext().getResources().getString(R.string.no_title);
        }
        return title;
    }

    public int getColor() {
        return event.getColor();
    }

    public boolean isAllDay() {
        return allDay;
    }

    @Override
    public String getLocation() {
        return event.getLocation();
    }

    public boolean isAlarmActive() {
        return event.isAlarmActive();
    }

    public boolean isRecurring() {
        return event.isRecurring();
    }

    public boolean isPartOfMultiDayEvent() {
        return getEvent().isPartOfMultiDayEvent();
    }

    public boolean isStartOfMultiDayEvent() {
        return isPartOfMultiDayEvent() && !getEvent().getStartDate().isBefore(entryDate);
    }

    public boolean isEndOfMultiDayEvent() {
        return isPartOfMultiDayEvent() && isLastEntryOfEvent();
    }

    public boolean spansOneFullDay() {
        return entryDate.plusDays(1).isEqual(event.getEndDate());
    }

    public CalendarEvent getEvent() {
        return event;
    }

    @Override
    public String getEventTimeString() {
        return hideEventTime() ? "" : createTimeSpanString();
    }

    private boolean hideEventTime() {
        return spansOneFullDay() && !(isStartOfMultiDayEvent() || isEndOfMultiDayEvent()) ||
                isAllDay();
    }

    private String createTimeSpanString() {
        String startStr;
        String endStr;
        String separator = SPACE_DASH_SPACE;
        if (!isDateDefined(entryDate) || (isPartOfMultiDayEvent() && DateUtil.isMidnight(entryDate)
                && !isStartOfMultiDayEvent())) {
            startStr = ARROW;
            separator = SPACE;
        } else {
            startStr = DateUtil.formatTime(this::getSettings, entryDate);
        }
        if (getSettings().getShowEndTime()) {
            if (!isDateDefined(event.getEndDate()) || (isPartOfMultiDayEvent() && !isLastEntryOfEvent())) {
                endStr = ARROW;
                separator = SPACE;
            } else {
                endStr = DateUtil.formatTime(this::getSettings, event.getEndDate());
            }
        } else {
            separator = DateUtil.EMPTY_STRING;
            endStr = DateUtil.EMPTY_STRING;
        }

        if (startStr.equals(endStr)) {
            return startStr;
        }

        return startStr + separator + endStr;
    }

    public Context getContext() {
        return event.getContext();
    }

    public InstanceSettings getSettings() {
        return event.getSettings();
    }

    @Override
    public OrderedEventSource getSource() {
        return event.getEventSource();
    }

    @Override
    public String toString() {
        return super.toString() + " CalendarEntry ["
                + "allDay=" + allDay
                + ", time=" + getEventTimeString()
                + ", location=" + getLocationString()
                + ", event=" + event
                + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarEntry that = (CalendarEntry) o;
        return event.equals(that.event) && entryDate.equals(that.entryDate);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += 31 * event.hashCode();
        result += 31 * entryDate.hashCode();
        return result;
    }
}
