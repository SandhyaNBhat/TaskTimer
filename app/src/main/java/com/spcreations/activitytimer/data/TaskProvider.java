package com.spcreations.activitytimer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TaskProvider extends ContentProvider {

    //Database Helper Object

    private TaskDBHelper mDBHelper;

   //URI matcher codes for the content URI for the tasks and subtasks table

    private static final int TASKS = 100;
    private static final int SUBTASKS = 200;

  //URI matcher codes for the content URI for the single task in tasks and subtasks table

    private static final int TASK_ID = 101;
    private static final int SUBTASK_ID = 201;

    /** Tag for the log messages */
    public static final String LOG_TAG = TaskProvider.class.getSimpleName();


    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static{

        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

         sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY,TaskContract.PATH_TASKS,TASKS);
         sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY,TaskContract.PATH_TASKS+"/#",TASK_ID);
         sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY,TaskContract.PATH_SUB_TASKS,SUBTASKS);
         sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY,TaskContract.PATH_SUB_TASKS+"/#",SUBTASK_ID);

    }



    @Override
    public boolean onCreate() {

        mDBHelper = new TaskDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        Cursor cursor;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case TASK_ID:
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SUBTASKS:
                cursor = db.query(TaskContract.SubTaskEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SUBTASK_ID:
                selection = TaskContract.SubTaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor =  db.query(TaskContract.SubTaskEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI "+uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                return insertTask(uri, contentValues);
            case SUBTASKS:
                return insertSubTask(uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    /**
     * Insert a task into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertTask(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(TaskContract.TaskEntry.COLUMN_TASK_NAME);
        if (name == null||name.isEmpty()) {
            throw new IllegalArgumentException("Activity requires a name");
        }



        SQLiteDatabase db = mDBHelper.getWritableDatabase();


        long id = db.insert(TaskContract.TaskEntry.TABLE_NAME,null,values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    /**
     * Insert a task into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSubTask(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME);
        if (name == null||name.isEmpty()) {
            throw new IllegalArgumentException("Sub Task requires a name");
        }

        // Check that the timer field value is not null or 0
        String timer = values.getAsString(TaskContract.SubTaskEntry.COLUMN_TIMER);
        if (timer == null||timer.isEmpty()||Integer.parseInt(timer)==0) {
            throw new IllegalArgumentException("Sub Task timer value should be greater than 0 and it should not be null");
        }



        SQLiteDatabase db = mDBHelper.getWritableDatabase();


        long id = db.insert(TaskContract.SubTaskEntry.TABLE_NAME,null,values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(TaskContract.TaskEntry.TABLE_NAME,selection,selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;


            case TASK_ID:
                // Delete a single row given by the ID in the URI
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted = database.delete(TaskContract.TaskEntry.TABLE_NAME,selection,selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    selection = TaskContract.SubTaskEntry.COLUMN_TASK_ID +"=?";
                    int rowsDeletedST = database.delete(TaskContract.SubTaskEntry.TABLE_NAME,selection,selectionArgs);

                }

                return rowsDeleted;

            case SUBTASK_ID:
                // Delete a single row subtask given by the ID in the URI
                selection = TaskContract.SubTaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted = database.delete(TaskContract.SubTaskEntry.TABLE_NAME,selection,selectionArgs);

                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;




            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return updateActivity(uri, contentValues, selection, selectionArgs);
            case TASK_ID:
                // For the TASK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateActivity(uri, contentValues, selection, selectionArgs);

            case SUBTASK_ID:
                // For the TASK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                Log.e("LOGTAG","Inside the update function");
                selection = TaskContract.SubTaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                Log.e("LOGTAG","selection ="+selection);
                Log.e("LOGTAG","selectionArgs ="+selectionArgs);
                return updateActivity(uri, contentValues, selection, selectionArgs);



            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Update Activities in the database with the given content values. Apply the changes to the rows
     * Return the number of rows that were successfully updated.
     */
    private int updateActivity(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.e("LOGTAG","Inside the updateActivity function");
        final int match = sUriMatcher.match(uri);
        int numOfRows;

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            Log.e("LOGTAG","No values to update");
            return 0;
        }

        switch (match) {

            case SUBTASK_ID:
                Log.e("LOGTAG","Inside the subtask_id logic of updateActivity function");

                selection = TaskContract.SubTaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

             int numOfRowsST = db.update(TaskContract.SubTaskEntry.TABLE_NAME,values,selection,selectionArgs);
                if (numOfRowsST != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numOfRowsST;

            default:
                Log.e("LOGTAG","Inside the default logic of updateActivity function");

                // If the {@link TaskEntry#COLUMN_TIME_REMINDER} value is present,
                // check that the timer value is valid.
                if (values.containsKey(TaskContract.TaskEntry.COLUMN_TIME_REMINDER)) {
                    // Check that the timer is greater than 0
                    Integer timer = values.getAsInteger(TaskContract.TaskEntry.COLUMN_TIME_REMINDER);
                    if (timer != null && timer < 0) {
                        throw new IllegalArgumentException("Timer value should be greater than 0");
                    }
                    if (timer == null){
                        throw new IllegalArgumentException("Timer value cannot be null. Enter valid value");

                    }
                }

                numOfRows = db.update(TaskContract.TaskEntry.TABLE_NAME,values,selection,selectionArgs);
                if (numOfRows != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numOfRows;

        }
    }

}
