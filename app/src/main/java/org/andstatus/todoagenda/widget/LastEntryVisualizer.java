package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.TextShadingPref;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

/** @author yvolk@yurivolkov.com */
public class LastEntryVisualizer extends WidgetEntryVisualizer<LastEntry> {
    private static final String TAG = LastEntryVisualizer.class.getSimpleName();

    public LastEntryVisualizer(Context context, int widgetId) {
        super(new EventProvider(EventProviderType.EMPTY, context, widgetId));
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        if(!(eventEntry instanceof LastEntry)) return null;

        LastEntry entry = (LastEntry) eventEntry;
        Log.d(TAG, "lastEntry: " + entry.type);
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), entry.type.layoutId);

        int viewId = R.id.event_entry;
        if (position >= 0) {
            rv.setOnClickFillInIntent(viewId, getIntent(entry));
        } else {
            rv.setOnClickPendingIntent(R.id.event_entry,
                    PermissionsUtil.getNoPermissionsPendingIntent(getSettings()));
        }
        if (entry.type == LastEntry.LastEntryType.EMPTY && getSettings().noPastEvents()) {
            rv.setTextViewText(viewId, getContext().getText(R.string.no_upcoming_events));
        }
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_title);
        setTextColorFromAttr(getSettings().getShadingContext(TextShadingPref.forTitle(entry)),
                rv, viewId, R.attr.eventEntryTitle);
        setBackgroundColor(rv, viewId, getSettings().getEntryBackgroundColor(entry));
        return rv;
    }

    private Intent getIntent(LastEntry entry) {
        switch (entry.type) {
            case EMPTY:
            case NOT_LOADED:
                if (PermissionsUtil.arePermissionsGranted(getSettings().getContext())) {
                    return createOpenCalendarAtDayIntent(new DateTime(getSettings().clock().getZone()));
                }
                break;
            default:
                break;
        }
        return  MainActivity.intentToStartMe(getSettings().getContext());
    }

    @Override
    public List<LastEntry> queryEventEntries() {
        return Collections.emptyList();
    }
}