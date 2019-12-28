package org.andstatus.todoagenda.widget;

import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;

import static org.andstatus.todoagenda.util.DateUtil.isSameDate;

public abstract class WidgetEntry<T extends WidgetEntry<T>> implements Comparable<WidgetEntry<T>> {

    public final WidgetEntryPosition entryPosition;
    public final DateTime entryDate;
    @Nullable
    public final DateTime endDate;
    public final boolean isLastEntryOfEvent;

    protected WidgetEntry(WidgetEntryPosition entryPosition, DateTime entryDate, @Nullable DateTime eventEndDate) {
        this.entryPosition = entryPosition;
        this.entryDate = fixEntryDate(entryPosition, entryDate);
        endDate = eventEndDate;
        isLastEntryOfEvent = endDate == null ||
                !entryPosition.entryDateIsRequired ||
                endDate.isBefore(DateUtil.startOfNextDay(this.entryDate));
    }

    private static DateTime fixEntryDate(WidgetEntryPosition entryPosition, DateTime entryDate) {
        switch (entryPosition) {
            case ENTRY_DATE:
                throwIfNull(entryPosition, entryDate);
                return entryDate;
            case PAST_AND_DUE_HEADER:
                return entryDate == null
                        ? DateUtil.DATETIME_MIN
                        : entryDate;
            case DAY_HEADER:
            case START_OF_DAY:
                throwIfNull(entryPosition, entryDate);
                return entryDate.withTimeAtStartOfDay();
            case START_OF_TODAY:
                return DateUtil.isToday(entryDate)
                            ? entryDate
                            : DateUtil.DATETIME_MIN;
            case END_OF_TODAY:
                return DateUtil.isToday(entryDate)
                            ? entryDate
                            : DateUtil.DATETIME_MAX;
            case END_OF_LIST_HEADER:
            case END_OF_LIST:
            case LIST_FOOTER:
                return entryDate == null
                        ? DateUtil.DATETIME_MAX
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

    public DateTime getEntryDay() {
        return entryDate.withTimeAtStartOfDay();
    }

    public DateTime getEndDate() {
        return endDate;
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
        return Days.daysBetween(DateUtil.now(entryDate.getZone()).withTimeAtStartOfDay(),
                entryDate.withTimeAtStartOfDay()).getDays();
    }

    @Override
    public int compareTo(WidgetEntry other) {
        int globalSignum = Integer.signum(entryPosition.globalOrder - other.entryPosition.globalOrder);
        if (globalSignum != 0) return globalSignum;

        int sameDaySignum = Integer.signum(entryPosition.sameDayOrder - other.entryPosition.sameDayOrder);
        if ((sameDaySignum != 0) && DateUtil.isSameDay(entryDate, other.entryDate)) return sameDaySignum;

        if (entryDate.isAfter(other.entryDate)) {
            return 1;
        } else if (entryDate.isBefore(other.entryDate)) {
            return -1;
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
        if (DateUtil.isToday(entryDate)) {
            switch (entryPosition) {
                case DAY_HEADER:
                    return TimeSection.TODAY;
                default:
                    if (DateUtil.isToday(getEndDate())) {
                        return DateUtil.isBeforeNow(getEndDate())
                                ? TimeSection.PAST
                                : TimeSection.TODAY;
                    }
            }
        }
        return DateUtil.isBeforeToday(entryDate)
                ? TimeSection.PAST
                : (DateUtil.isToday(endDate) ? TimeSection.TODAY : TimeSection.FUTURE);
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
                (entryDate == DateUtil.DATETIME_MIN ? "min" :
                        (entryDate == DateUtil.DATETIME_MAX) ? "max" : entryDate) +
                "]";
    }
}
