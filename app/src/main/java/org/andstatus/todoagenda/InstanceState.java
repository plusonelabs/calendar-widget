package org.andstatus.todoagenda;

import androidx.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yvolk@yurivolkov.com
 */
public class InstanceState {
    private final static ConcurrentHashMap<Integer, InstanceState> instances = new ConcurrentHashMap<>();
    public final static InstanceState EMPTY = new InstanceState(0, 0, 0);

    public final long updated;
    public final long listReloaded;
    public final long listRedrawn;

    private InstanceState(long updated, long listReloaded, long listRedrawn) {
        this.updated = updated;
        this.listReloaded = listReloaded;
        this.listRedrawn = listRedrawn;
    }

    public static void clearAll() {
        instances.clear();
    }

    public static void clear(@NonNull Integer widgetId) {
        instances.remove(widgetId);
    }

    public static void updated(@NonNull Integer widgetId) {
        instances.compute(widgetId, (id, state) -> new InstanceState(
                state == null ? 1 : state.updated + 1,
                state == null ? 0 : state.listReloaded,
                state == null ? 0 : state.listRedrawn)
        );
    }

    public static void listReloaded(@NonNull Integer widgetId) {
        instances.compute(widgetId, (id, state) -> new InstanceState(
                state == null ? 0 : state.updated,
                state == null ? 1 : state.listReloaded + 1,
                state == null ? 0 : state.listRedrawn)
        );
    }

    public static void listRedrawn(@NonNull Integer widgetId) {
        instances.compute(widgetId, (id, state) -> new InstanceState(
                state == null ? 0 : state.updated,
                state == null ? 0 : state.listReloaded,
                state == null ? 1 : state.listRedrawn + 1)
        );
    }

    public static InstanceState get(Integer widgetId) {
        return instances.getOrDefault(widgetId, EMPTY);
    }
}