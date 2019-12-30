package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.LazyVal;
import org.andstatus.todoagenda.util.PermissionsUtil;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Testing and mocking Calendar and Tasks Providers
 *
 * @author yvolk@yurivolkov.com
 */
public class MyContentResolver {
    private final static String TAG = MyContentResolver.class.getSimpleName();
    final EventProviderType type;
    final Context context;
    final int widgetId;
    private final AtomicInteger requestsCounter = new AtomicInteger();
    private final LazyVal<InstanceSettings> settingsLazyVal;

    public MyContentResolver(EventProviderType type, Context context, int widgetId) {
        this.type = type;
        this.context = context;
        this.widgetId = widgetId;
        settingsLazyVal = LazyVal.ofNullable(() -> AllSettings.instanceFromId(context, widgetId));
    }

    @NonNull
    public InstanceSettings getSettings() {
        return settingsLazyVal.get();
    }

    public boolean isPermissionNeeded(Context context, String permission) {
        return PermissionsUtil.isPermissionNeeded(context, permission);
    }

    public <R> R foldAvailableSources(@NonNull Uri uri, @Nullable String[] projection,
                            R identity, Function<R, Function<Cursor, R>> foldingFunction) {
        R folded = identity;
        try (Cursor cursor = queryAvailableSources(uri, projection)) {
            if (cursor != null) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    folded = foldingFunction.apply(folded).apply(cursor);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.d(type.name(), widgetId + " " + e.getMessage());
        } catch (Exception e) {
            Log.w(type.name(), widgetId + " Failed to fetch available sources" +
                            " uri:" + uri +
                            ", projection:" + Arrays.toString(projection), e);
        }
        return folded;
    }

    private Cursor queryAvailableSources(@NonNull Uri uri, @Nullable String[] projection) {
        try {
            return widgetId == 0 || getSettings().getQueryResults() == null
                    ? context.getContentResolver().query(uri, projection, null, null, null)
                    : getSettings().getQueryResults().getResult(type, requestsCounter.incrementAndGet() - 1)
                    .map(r -> r.querySource(projection)).orElse(null);
        } catch (Exception e) {
            Log.d(TAG, "Failed to get available sources for " + uri + "; " + e.getMessage());
            return null;
        }
    }

    public void onQueryEvents() {
        requestsCounter.set(0);
    }

    public <R> R foldEvents(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                          @Nullable String[] selectionArgs, @Nullable String sortOrder,
                                  R identity, Function<R, Function<Cursor, R>> foldingFunction) {
        R folded = identity;
        boolean needToStoreResults = QueryResultsStorage.getNeedToStoreResults(widgetId);
        QueryResult result = needToStoreResults
                ? new QueryResult(type, getSettings(), uri, projection, selection, null, sortOrder)
                : null;
        try (Cursor cursor = queryForEvents(uri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    if (needToStoreResults) result.addRow(cursor);
                    folded = foldingFunction.apply(folded).apply(cursor);
                }
            }
        } catch (Exception e) {
            Log.w(type.name(), widgetId + " Failed to query events" +
                    " uri:" + uri +
                    ", projection:" + Arrays.toString(projection) +
                    ", selection:" + selection +
                    ", args:" + Arrays.toString(selectionArgs) +
                    ", sort:" + sortOrder, e);
        }
        if (needToStoreResults) QueryResultsStorage.store(result.dropNullColumns());
        return folded;
    }

    private Cursor queryForEvents(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                                  @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return getSettings().getQueryResults() == null
                ? context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)
                : getSettings().getQueryResults().getResult(type, requestsCounter.incrementAndGet() - 1)
                    .map(r -> r.query(projection)).orElse(null);
    }
}
