package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;

import java.util.List;

import static org.andstatus.todoagenda.widget.LastEntry.LastEntryType.EMPTY;
import static org.andstatus.todoagenda.widget.LastEntry.LastEntryType.NO_PERMISSIONS;

/** @author yvolk@yurivolkov.com */
public class LastEntry extends WidgetEntry<LastEntry> {

    public static LastEntry forEmptyList(InstanceSettings settings) {
        LastEntry.LastEntryType entryType = PermissionsUtil.arePermissionsGranted(settings.getContext())
                ? EMPTY
                : NO_PERMISSIONS;
        return new LastEntry(entryType, DateUtil.now(settings.getTimeZone()));
    }

    public static void addLast(List<WidgetEntry> widgetEntries) {
        if (!widgetEntries.isEmpty()) {
            LastEntry entry = new LastEntry(LastEntryType.LAST, widgetEntries.get(widgetEntries.size() - 1).getStartDate());
            widgetEntries.add(entry);
        }
    }

    public enum LastEntryType {
        NOT_LOADED(R.layout.item_not_loaded),
        NO_PERMISSIONS(R.layout.item_no_permissions),
        EMPTY(R.layout.item_empty_list),
        LAST(R.layout.item_last);

        final int layoutId;

        LastEntryType(int layoutId) {
            this.layoutId = layoutId;
        }
    }

    public final LastEntryType type;

    public LastEntry(LastEntryType type, DateTime date) {
        this.type = type;
        super.setStartDate(date);
    }
}
