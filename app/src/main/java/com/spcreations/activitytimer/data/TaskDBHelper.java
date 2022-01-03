package com.spcreations.activitytimer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "activitytimer.db";
    public static final int DATABASE_VERSION =1;

    private static final String TEXT_TYPE = " TEXT ";
    private static final String COMMA_SEP = ",";

    /** SQL statements for Tasks table**/

    private static final String SQL_CREATE_TASKS =
            "CREATE TABLE "+ TaskContract.TaskEntry.TABLE_NAME +"("
                    + TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TaskContract.TaskEntry.COLUMN_TASK_NAME +" TEXT NOT NULL, "
                    + TaskContract.TaskEntry.COLUMN_SUBTASK + " TEXT, "
                    + TaskContract.TaskEntry.COLUMN_TIME_REMINDER+ " TEXT );";

    private static final String SQL_DELETE_TASKS ="DROP TABLE IF EXISTS "+ TaskContract.TaskEntry.TABLE_NAME;


    /** SQL statements for SubTasks table**/

    private static final String SQL_CREATE_SUBTASKS =
            "CREATE TABLE "+ TaskContract.SubTaskEntry.TABLE_NAME +"("
                    + TaskContract.SubTaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +TaskContract.SubTaskEntry.COLUMN_TASK_ID +" TEXT,"
                    + TaskContract.SubTaskEntry.COLUMN_TASK_NAME +" TEXT, "
                    + TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME + " TEXT, "
                    + TaskContract.SubTaskEntry.COLUMN_TIMER + " TEXT, "
                    +TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT + " TEXT, "
                    + TaskContract.SubTaskEntry.COLUMN_TIMER_SECS +" TEXT );";

    private static final String SQL_DELETE_SUBTASKS ="DROP TABLE IF EXISTS "+ TaskContract.SubTaskEntry.TABLE_NAME;

    public TaskDBHelper(Context context) { super (context,DATABASE_NAME,null,DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASKS);
        db.execSQL(SQL_CREATE_SUBTASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TASKS);
        db.execSQL(SQL_DELETE_SUBTASKS);
        onCreate(db);

    }
}

