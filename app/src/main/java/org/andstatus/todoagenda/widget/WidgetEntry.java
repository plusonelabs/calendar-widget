package org.andstatus.todoagenda.widget;

import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.MyClock;
import org.joda.time.DateTime;
import org.joda.time.Days;

import static org.andstatus.todoagenda.util.DateUtil.isSameDate;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.END_OF_LIST;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.ENTRY_DATE;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.PAST_AND_DUE;

public abstract class WidgetEntry<T extends WidgetEntry<T>> implements Comparable<WidgetEntry<T>> {

    public final WidgetEntryPosition entryPosition;
    public final DateTime entryDate;
    protected final InstanceSettings settings;

    protected WidgetEntry(InstanceSettings settings, WidgetEntryPosition entryPosition, DateTime entryDate) {
        this.settings = settings;
        this.entryPosition = entryPosition;
        this.entryDate = fixEntryDate(entryPosition, entryDate);
    }

    private DateTime fixEntryDate(WidgetEntryPosition entryPosition, DateTime entryDate) {
        switch (entryPosition) {
            case ENTRY_DATE:
                throwIfNull(entryPosition, entryDate);
                return entryDate;
            case PAST_AND_DUE_HEADER:
            case PAST_AND_DUE:
            case START_OF_TODAY:
            case HIDDEN:
                return entryDate == null
                        ? MyClock.DATETIME_MIN
                        : entryDate;
            case DAY_HEADER:
            case START_OF_DAY:
                throwIfNull(entryPosition, entryDate);
                return entryDate.withTimeAtStartOfDay();
            case END_OF_TODAY:
            case END_OF_LIST_HEADER:
            case END_OF_LIST:
            case LIST_FOOTER:
                return entryDate == null
                        ? MyClock.DATETIME_MAX
                        : entryDate;
            default:
                throw new IllegalArgumentException("Invalid position " + entryPosition + "; entryDate: " + entryDate);
        }
    }

    private static void throwIfNull(WidgetEntryPosition entryPosition, DateTime entryDate) {
        if (entryDate == null) {
            throw new IllegalArgumentException("Invalid entry date: " + entryDate + " at position " + entryPosition);
        }
    }

    public boolean isLastEntryOfEvent() {
        return getEndDate() == null ||
                !entryPosition.entryDateIsRequired ||
                getEndDate().isBefore(MyClock.startOfNextDay(this.entryDate));
    }

    public static WidgetEntryPosition getEntryPosition(InstanceSettings settings, DateTime mainDate, DateTime otherDate) {
        if (mainDate == null && otherDate == null) return settings.getTaskWithoutDates().widgetEntryPosition;

        DateTime refDate = mainDate == null ? otherDate : mainDate;
        if (settings.getShowPastEventsUnderOneHeader() && settings.clock().isBeforeToday(refDate)) {
            return PAST_AND_DUE;
        }
        if (refDate.isAfter(settings.getEndOfTimeRange())) return END_OF_LIST;
        return ENTRY_DATE;
    }

    public DateTime getEntryDay() {
        switch (entryPosition) {
            case START_OF_TODAY:
            case END_OF_TODAY:
                return settings.clock().now().withTimeAtStartOfDay();
            default:
                return entryDate.withTimeAtStartOfDay();
        }
    }

    @Nullable
    public DateTime getEndDate() {
        return null;
    }

    public OrderedEventSource getSource() {
        return OrderedEventSource.EMPTY;
    }

    public String getTitle() {
        return "";
    }

    public String getLocation() {
        return "";
    }

    public int getDaysFromToday() {
        return Days.daysBetween(settings.clock().now(entryDate.getZone()).withTimeAtStartOfDay(),
                entryDate.withTimeAtStartOfDay()).getDays();
    }

    @Override
    public int compareTo(WidgetEntry other) {
        int globalSignum = Integer.signum(entryPosition.globalOrder - other.entryPosition.globalOrder);
        if (globalSignum != 0) return globalSignum;

        if (DateUtil.isSameDay(getEntryDay(), other.getEntryDay())) {
            int sameDaySignum = Integer.signum(entryPosition.sameDayOrder - other.entryPosition.sameDayOrder);
            if ((sameDaySignum != 0) && DateUtil.isSameDay(getEntryDay(), other.getEntryDay())) return sameDaySignum;

            if (entryDate.isAfter(other.entryDate)) {
                return 1;
            } else if (entryDate.isBefore(other.entryDate)) {
                return -1;
            }
        } else {
            if (getEntryDay().isAfter(other.getEntryDay())) {
                return 1;
            } else if (getEntryDay().isBefore(other.getEntryDay())) {
                return -1;
            }
        }

        int sourceSignum = Integer.signum(getSource().order - other.getSource().order);
        return sourceSignum == 0
                ? getTitle().compareTo(other.getTitle())
                : sourceSignum;
    }

    public TimeSection getTimeSection() {
        switch (entryPosition) {
            case PAST_AND_DUE_HEADER:
                return TimeSection.PAST;
            case START_OF_TODAY:
                return TimeSection.TODAY;
            case END_OF_TODAY:
            case END_OF_LIST_HEADER:
            case END_OF_LIST:
            case LIST_FOOTER:
                return TimeSection.FUTURE;
            default:
                break;
        }
        if (settings.clock().isToday(entryDate)) {
            switch (entryPosition) {
                case DAY_HEADER:
                    return TimeSection.TODAY;
                default:
                    if (settings.clock().isToday(getEndDate())) {
                        return settings.clock().isBeforeNow(getEndDate())
                                ? TimeSection.PAST
                                : TimeSection.TODAY;
                    }
            }
        }
        return settings.clock().isBeforeToday(entryDate)
                ? TimeSection.PAST
                : (settings.clock().isToday(getEndDate()) ? TimeSection.TODAY : TimeSection.FUTURE);
    }

    public boolean duplicates(WidgetEntry other) {
        return entryPosition == other.entryPosition &&
            entryDate.equals(other.entryDate) &&
            isSameDate(getEndDate(), other.getEndDate()) &&
            getTitle().equals(other.getTitle()) &&
            getLocation().equals(other.getLocation());
    }

    @Override
    public String toString() {
        return entryPosition.value + " [entryDate=" +
                (entryDate == MyClock.DATETIME_MIN ? "min" :
                        (entryDate == MyClock.DATETIME_MAX) ? "max" : entryDate) +
                "]";
    }
}
