package com.spcreations.activitytimer.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class TaskContract {

    private TaskContract(){}

    public static final String CONTENT_AUTHORITY = "com.spcreations.activitytimer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TASKS = "tasks";
    public static final String PATH_SUB_TASKS = "sub_tasks";




    public static abstract class TaskEntry implements  BaseColumns {

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/" +CONTENT_AUTHORITY + "/"
                + PATH_TASKS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_TASKS;


        /*The content URI to access the task data in the provider */

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TASKS);


        public static final String TABLE_NAME = "tasks";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TASK_NAME = "task_name";
        public static final String COLUMN_SUBTASK = "subtask_present";
        public static final String COLUMN_TIME_REMINDER = "task_timer";

        public static final int SUBTASK_YES= 1;
        public static final int SUBTASK_NO = 0;

        public static final int TIMER_MINS = 1;
        public static final int TIMER_SECS = 0;

    }

    public static abstract class SubTaskEntry implements  BaseColumns {

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/" +CONTENT_AUTHORITY + "/"
                + PATH_SUB_TASKS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_SUB_TASKS;


        /*The content URI to access the task data in the provider */

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUB_TASKS);


        public static final String TABLE_NAME = "sub_tasks";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_TASK_NAME = "task_name";
        public static final String COLUMN_SUBTASK_NAME = "subtask_name";
        public static final String COLUMN_TIMER = "subtask_timer";
        public static final String COLUMN_TIMER_UNIT ="timer_unit";
        public static final String COLUMN_TIMER_SECS = "timer_in_secs";

    }






}
