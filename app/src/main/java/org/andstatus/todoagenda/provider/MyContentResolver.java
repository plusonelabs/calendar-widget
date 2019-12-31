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

import io.vavr.control.Try;

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
        return getSettings().isLiveMode() && PermissionsUtil.isPermissionNeeded(context, permission);
    }

    public <R> Try<R> foldAvailableSources(@NonNull Uri uri, @Nullable String[] projection,
                                           R identity, Function<R, Function<Cursor, R>> foldingFunction) {
        R folded = identity;
        try (Cursor cursor = queryAvailableSources(uri, projection)) {
            if (cursor != null) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    folded = foldingFunction.apply(folded).apply(cursor);
                }
            }
        } catch (SecurityException e) {
            return Try.failure(e);
        } catch (IllegalArgumentException e) {
            Log.d(type.name(), widgetId + " " + e.getMessage());
        } catch (Exception e) {
            Log.w(type.name(), widgetId + " Failed to fetch available sources" +
                            " uri:" + uri +
                            ", projection:" + Arrays.toString(projection), e);
        }
        return Try.success(folded);
    }

    private Cursor queryAvailableSources(@NonNull Uri uri, @Nullable String[] projection) {
        return widgetId == 0 || getSettings().isLiveMode()
                ? context.getContentResolver().query(uri, projection, null, null, null)
                : getSettings().getResultsStorage().getResult(type, requestsCounter.incrementAndGet() - 1)
                .map(r -> r.querySource(projection)).orElse(null);
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
        return getSettings().isLiveMode()
                ? context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)
                : getSettings().getResultsStorage().getResult(type, requestsCounter.incrementAndGet() - 1)
                    .map(r -> r.query(projection)).orElse(null);
    }
}
