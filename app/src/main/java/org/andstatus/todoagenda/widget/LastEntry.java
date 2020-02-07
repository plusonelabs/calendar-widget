package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;

import java.util.List;

import static org.andstatus.todoagenda.widget.LastEntry.LastEntryType.EMPTY;
import static org.andstatus.todoagenda.widget.LastEntry.LastEntryType.NO_PERMISSIONS;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.LIST_FOOTER;

/** @author yvolk@yurivolkov.com */
public class LastEntry extends WidgetEntry<LastEntry> {

    public static LastEntry forEmptyList(InstanceSettings settings) {
        LastEntry.LastEntryType entryType = PermissionsUtil.arePermissionsGranted(settings.getContext())
                ? EMPTY
                : NO_PERMISSIONS;
        return new LastEntry(settings, entryType, settings.clock().now());
    }

    public static void addLast(InstanceSettings settings, List<WidgetEntry> widgetEntries) {
        LastEntry entry = widgetEntries.isEmpty()
            ? LastEntry.forEmptyList(settings)
            : new LastEntry(settings, LastEntryType.LAST, widgetEntries.get(widgetEntries.size() - 1).entryDate);
        widgetEntries.add(entry);
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

    public LastEntry(InstanceSettings settings, LastEntryType type, DateTime date) {
        super(settings, LIST_FOOTER, date, null);
        this.type = type;
    }
}