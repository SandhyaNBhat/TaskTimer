package com.spcreations.activitytimer;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spcreations.activitytimer.data.TaskContract;
import com.spcreations.activitytimer.data.TaskDBHelper;

import java.util.HashMap;

import static android.view.View.GONE;

public class TaskListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ActivityAdapter mCursorAdapter;
    private static final int TASK_LOADER = 0;
    TaskDBHelper mDBHelper;
    Cursor mCursor;


    private final String LOG_TAG = getClass().getSimpleName().toString();

    private static final String[] TASK_PROJECTION = new String[] {
            TaskContract.TaskEntry._ID,
            TaskContract.TaskEntry.COLUMN_TASK_NAME };
    private static final String[] SUBTASK_PROJECTION = new String[] {
            TaskContract.SubTaskEntry._ID,
            TaskContract.SubTaskEntry.COLUMN_TASK_NAME,
            TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        View emptyView = findViewById(R.id.empty_view);
        emptyView.setVisibility(GONE);
        RecyclerView taskListView = findViewById(R.id.activityList);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TaskListActivity.this, AddTasksActivity.class);
                startActivity(intent);
            }
        });

        Log.e("LOGTAG","Inside TaskListActivity");

        mCursor=getData();
        if (mCursor.getCount()==0){
            emptyView.setVisibility(View.VISIBLE);
        }

        //use ListView and Cursoradapter to display the task names in Task List screen


        taskListView.setLayoutManager(new LinearLayoutManager(this));

        Log.e("LOGTAG","Inside TaskListActivity, set activityAdapter");
        mCursorAdapter = new ActivityAdapter(this,getData());
       // mCursorAdapter = new TaskAdapter(this);
        taskListView.setAdapter(mCursorAdapter);

        mCursorAdapter.setOnItemClickListener(new ActivityAdapter.onItemClickListener() {
            @Override
            public void onEditClick(int position,long l) {
                Log.e("LOGTAG","Position of the item "+position+"  "+l);


                Intent intent = new Intent(TaskListActivity.this,AddTasksActivity.class);

               Uri uri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI,l);
                //Toast.makeText(TaskListActivity.this, "uri "+uri, Toast.LENGTH_SHORT).show();
                //Log.e(LOG_TAG,"Uri -"+uri);
            intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onStartClick(int position,long l) {

                Intent i = new Intent(TaskListActivity.this,StartActivity.class);

                //Fetch taskname and id for the clicked one.

                String[] projection = {TaskContract.TaskEntry._ID,
                        TaskContract.TaskEntry.COLUMN_TASK_NAME};

                String selection = TaskContract.TaskEntry._ID + "=?";

                String[] selectionArgs = {Long.toString(l)};

                Cursor cursor = getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,projection,selection,selectionArgs,null);

                String taskname,taskId;

                try{
                    while(cursor.moveToNext()) {
                        taskname = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                        taskId = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry._ID));
                        i.putExtra("task_name",taskname);
                        i.putExtra("task_id",taskId);
                        Log.e("LOGTAG","Start activity for -"+taskname+" "+taskId);
                    }
                }finally {
                    cursor.close();
                }


                //Toast.makeText(TaskListActivity.this, "uri "+uri, Toast.LENGTH_SHORT).show();
                // i.setData(uri);
                startActivity(i);

            }
        });



       LoaderManager.getInstance(this).initLoader(TASK_LOADER,null,this);





        //Set up item click listener to edit the task details or to add subtasks to the existing task

     /*  taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TaskListActivity.this);
                builder.setMessage(R.string.edit_or_start_task);
                builder.setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    Intent intent = new Intent(TaskListActivity.this,AddTasksActivity.class);

                        Uri uri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI,l);
                        //Toast.makeText(TaskListActivity.this, "uri "+uri, Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG,"Uri -"+uri);
                      intent.setData(uri);
                       startActivity(intent);
                    }
                });
                builder.setNegativeButton(R.string.start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Start" button, so start the activity
                        if (dialog != null) {
                            Intent i = new Intent(TaskListActivity.this,StartActivity.class);

                            //Fetch taskname and id for the clicked one.

                            String[] projection = {TaskContract.TaskEntry._ID,
                            TaskContract.TaskEntry.COLUMN_TASK_NAME};

                            String selection = TaskContract.TaskEntry._ID + "=?";

                            String[] selectionArgs = {Long.toString(l)};

                            Cursor cursor = getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,projection,selection,selectionArgs,null);

                           String taskname,taskId;

                           try{
                               while(cursor.moveToNext()) {
                                   taskname = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                                   taskId = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry._ID));
                                   i.putExtra("task_name",taskname);
                                   i.putExtra("task_id",taskId);
                               }
                           }finally {
                               cursor.close();
                           }


                            //Toast.makeText(TaskListActivity.this, "uri "+uri, Toast.LENGTH_SHORT).show();
                           // i.setData(uri);
                            startActivity(i);
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }
        });*/




    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable  Bundle args) {
        String[] projection = {TaskContract.TaskEntry._ID,
                TaskContract.TaskEntry.COLUMN_TASK_NAME};

        return new CursorLoader(this,
                TaskContract.TaskEntry.CONTENT_URI,
                projection,null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.e("LOGTAG","Inside OnloaderFinished");
        mCursorAdapter.swapCursor(getData());

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mCursorAdapter.swapCursor(null);

    }

    private Cursor getData(){

        Cursor c1 = getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,TASK_PROJECTION,null,null,null);
        return c1;
    }


    }
