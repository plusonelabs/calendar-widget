package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.PermissionsUtil;

import java.util.Arrays;
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
    private volatile int requestsCounter = 0;

    public MyContentResolver(EventProviderType type, Context context, int widgetId) {
        this.type = type;
        this.context = context;
        this.widgetId = widgetId;
    }

    @NonNull
    public InstanceSettings getSettings() {
        return AllSettings.instanceFromId(context, widgetId);
    }

    public boolean isPermissionNeeded(Context context, String permission) {
        return PermissionsUtil.isPermissionNeeded(context, permission);
    }

    public <R> R foldAvailableSources(@NonNull Uri uri, @Nullable String[] projection,
                            R identity, Function<R, Function<Cursor, R>> foldingFunction) {
        R folded = identity;
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
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

    public void onQueryEvents() {
        requestsCounter = 0;
    }

    public <R> R foldEvents(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                          @Nullable String[] selectionArgs, @Nullable String sortOrder,
                                  R identity, Function<R, Function<Cursor, R>> foldingFunction) {
        R folded = identity;
        boolean needToStoreResults = QueryResultsStorage.getNeedToStoreResults();
        QueryResult result = needToStoreResults
                ? new QueryResult(type, getSettings(), uri, projection, selection, null, sortOrder)
                : null;
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)) {
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
        if (needToStoreResults) QueryResultsStorage.store(result);
        return folded;
    }
}
