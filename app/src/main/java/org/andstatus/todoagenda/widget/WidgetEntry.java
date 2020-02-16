package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.MyClock;
import org.joda.time.DateTime;
import org.joda.time.Days;

import static org.andstatus.todoagenda.util.DateUtil.isSameDate;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.DAY_HEADER;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.END_OF_LIST;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.ENTRY_DATE;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.PAST_AND_DUE;

public abstract class WidgetEntry<T extends WidgetEntry<T>> implements Comparable<WidgetEntry<T>> {

    protected final InstanceSettings settings;
    public final WidgetEntryPosition entryPosition;
    public final DateTime entryDate;
    public final DateTime entryDay;
    public final DateTime endDate;
    public final TimeSection timeSection;

    protected WidgetEntry(InstanceSettings settings, WidgetEntryPosition entryPosition, DateTime entryDate, DateTime endDate) {
        this.settings = settings;
        this.entryPosition = entryPosition;
        this.endDate = endDate;
        this.entryDate = fixEntryDate(entryPosition, entryDate);
        entryDay = calcEntryDay(settings, entryPosition, this.entryDate);
        timeSection = calcTimeSection(settings, entryPosition, entryDay, endDate);
    }

    private static DateTime fixEntryDate(WidgetEntryPosition entryPosition, DateTime entryDate) {
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

    private static DateTime calcEntryDay(InstanceSettings settings, WidgetEntryPosition entryPosition, DateTime entryDate) {
        switch (entryPosition) {
            case START_OF_TODAY:
            case END_OF_TODAY:
                return settings.clock().now().withTimeAtStartOfDay();
            default:
                return entryDate.withTimeAtStartOfDay();
        }
    }

    private static TimeSection calcTimeSection(InstanceSettings settings, WidgetEntryPosition entryPosition,
                                               DateTime entryDay, DateTime endDate) {
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
        if (settings.clock().isToday(entryDay)) {
            if (entryPosition == DAY_HEADER) return TimeSection.TODAY;

            if (settings.clock().isToday(endDate)) {
                return settings.clock().isBeforeNow(endDate)
                        ? TimeSection.PAST
                        : TimeSection.TODAY;
            }
            return TimeSection.TODAY;
        }
        return settings.clock().isBeforeToday(entryDay)
                ? TimeSection.PAST
                : (settings.clock().isToday(endDate) ? TimeSection.TODAY : TimeSection.FUTURE);
    }

    private static void throwIfNull(WidgetEntryPosition entryPosition, DateTime entryDate) {
        if (entryDate == null) {
            throw new IllegalArgumentException("Invalid entry date: " + entryDate + " at position " + entryPosition);
        }
    }

    public boolean isLastEntryOfEvent() {
        return endDate == null ||
                !entryPosition.entryDateIsRequired ||
                endDate.isBefore(MyClock.startOfNextDay(this.entryDate));
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

    public String getEventTimeString() {
        return "";
    }

    public OrderedEventSource getSource() {
        return OrderedEventSource.EMPTY;
    }

    public String getTitle() {
        return "";
    }

    String getLocationString() {
        return hideLocation() ? "" : getLocation();
    }

    private boolean hideLocation() {
        return getLocation().isEmpty() || !settings.getShowLocation();
    }

    public String getLocation() {
        return "";
    }

    @Override
    public int compareTo(WidgetEntry other) {
        int globalSignum = Integer.signum(entryPosition.globalOrder - other.entryPosition.globalOrder);
        if (globalSignum != 0) return globalSignum;

        if (DateUtil.isSameDay(entryDay, other.entryDay)) {
            int sameDaySignum = Integer.signum(entryPosition.sameDayOrder - other.entryPosition.sameDayOrder);
            if ((sameDaySignum != 0) && DateUtil.isSameDay(entryDay, other.entryDay)) return sameDaySignum;

            if (entryDate.isAfter(other.entryDate)) {
                return 1;
            } else if (entryDate.isBefore(other.entryDate)) {
                return -1;
            }
        } else {
            if (entryDay.isAfter(other.entryDay)) {
                return 1;
            } else if (entryDay.isBefore(other.entryDay)) {
                return -1;
            }
        }

        int sourceSignum = Integer.signum(getSource().order - other.getSource().order);
        return sourceSignum == 0
                ? getTitle().compareTo(other.getTitle())
                : sourceSignum;
    }

    public boolean duplicates(WidgetEntry other) {
        return entryPosition == other.entryPosition &&
            entryDate.equals(other.entryDate) &&
            isSameDate(endDate, other.endDate) &&
            getTitle().equals(other.getTitle()) &&
            getLocation().equals(other.getLocation());
    }

    public CharSequence formatEntryDate() {
        return settings.getEntryDateFormat().type == DateFormatType.HIDDEN
                ? ""
                : settings.entryDateFormatter().formatDate(entryDate);
    }

    @Override
    public String toString() {
        return entryPosition.value + " [" +
                "entryDate=" +
                (entryDate == MyClock.DATETIME_MIN ? "min" :
                        (entryDate == MyClock.DATETIME_MAX) ? "max" : entryDate) +
                ", endDate=" + endDate +
            "]";
    }
}
