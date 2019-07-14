package org.andstatus.todoagenda.task;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class EmptyTaskProvider extends AbstractTaskProvider {

    public EmptyTaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    @Override
    public List<TaskEvent> getTasks() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public void requestPermission(Activity activity) {
        // No action necessary
    }
}
