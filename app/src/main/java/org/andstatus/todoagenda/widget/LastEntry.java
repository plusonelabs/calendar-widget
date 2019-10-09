package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;

import java.util.List;

/** @author yvolk@yurivolkov.com */
public class LastEntry extends WidgetEntry<LastEntry> {

    public static LastEntry from(InstanceSettings settings, List<WidgetEntry> widgetEntries) {
        return widgetEntries.isEmpty()
            ? new LastEntry(
                PermissionsUtil.arePermissionsGranted(settings.getContext())
                        ? LastEntryType.EMPTY
                        : LastEntryType.NO_PERMISSIONS,
                DateUtil.now(settings.getTimeZone()))
            : new LastEntry(LastEntryType.LAST, widgetEntries.get(widgetEntries.size() - 1).getStartDate());
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
