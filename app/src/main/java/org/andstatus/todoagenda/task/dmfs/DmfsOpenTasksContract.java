package org.andstatus.todoagenda.task.dmfs;

import android.net.Uri;

public class DmfsOpenTasksContract {
    public static final Uri PROVIDER_URI = Uri.parse("content://org.dmfs.tasks/tasks");

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DUE_DATE = "due";
    public static final String COLUMN_START_DATE = "dtstart";
    public static final String COLUMN_STATUS = "status";
    public static final int STATUS_COMPLETED = 2;

    public static final String PERMISSION = "org.dmfs.permission.READ_TASKS";
}
