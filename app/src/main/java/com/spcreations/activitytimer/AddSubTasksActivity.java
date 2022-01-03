package com.spcreations.activitytimer;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.spcreations.activitytimer.data.TaskContract;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.VISIBLE;

public class AddSubTasksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ImageView mRecordTask,mStopRecord;
    private Spinner mSpinner;
    private int mSpinnerVal;
    private Uri mCurrentUri;


    private String mTimerUnit,mainTaskName,mainTaskId,subtaskname,subtasktimer,timerunit,taskId,taskName;
    private EditText mSubTaskName,mSubTaskTimer;


    SubTaskAdapter mSubTaskAdapter;
    private static final int TASK_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sub_tasks);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        mCurrentUri = i.getData();

        if (mCurrentUri == null) {

            setTitle("Add SubActivity");
        }else{
            setTitle("Edit SubActivity");
            LoaderManager.getInstance(this).initLoader(TASK_LOADER,null,this);
        }

        Intent intent = getIntent();
         mainTaskName = intent.getStringExtra("task_name");
         mainTaskId = intent.getStringExtra("task_id");
         mSpinner = findViewById(R.id.spinnerfortimer);

        Log.e("LOGTAG","task -"+mainTaskName);



        mSubTaskName = findViewById(R.id.edit_subtaskname);
        mSubTaskTimer = findViewById(R.id.edit_subtasktimer);


        populateSpinner();


    }

    private void populateSpinner(){
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter valuesSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_values_timer, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        valuesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);


        // Apply the adapter to the spinner
        mSpinner.setAdapter(valuesSpinnerAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);



                if (mCurrentUri==null) {
                    Log.e("LOGTAG","Selection for new "+selection);
                    if (selection.equals(getString(R.string.value_mins))) {
                        Log.e("LOGTAG", "Selected " + selection);
                        mSpinnerVal = TaskContract.TaskEntry.TIMER_MINS;

                    } else {
                        Log.e("LOGTAG", "Selected " + selection);
                        mSpinnerVal = TaskContract.TaskEntry.TIMER_SECS;

                    }
                }else{
                    Log.e("LOGTAG","Selection for edit case "+selection);

                    if(selection.equals("Mins")){
                        Log.e("LOGTAG", "timer unit mins " + selection);
                        mSpinnerVal= TaskContract.TaskEntry.TIMER_MINS;
                    }else{
                        Log.e("LOGTAG", "timer unit secs" + selection);
                        mSpinnerVal = TaskContract.TaskEntry.TIMER_SECS;
                        Log.e("Logtag","mSpinnerVal "+mSpinnerVal);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSpinner.setSelection(0);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //this add menu items to the app bar
        getMenuInflater().inflate(R.menu.add_tasks,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save task to database
                saveSubTask();
                // Exit activity
                Log.e("LOGTAG","Calling Finish  after saveSubtask");
                finish();
                Log.e("LOGTAG","Finish called after saveSubtask");
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                //  showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(AddSubTasksActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(1).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    /* Get input from the SubTasks section and save details in sub_tasks table*/

    private void saveSubTask(){

        Log.e("LOGTAG","Inside saveSubTask for the URI "+TaskContract.SubTaskEntry.CONTENT_URI);




        mSubTaskName = findViewById(R.id.edit_subtaskname);
        mSubTaskTimer = findViewById(R.id.edit_subtasktimer);


        String subTaskName = mSubTaskName.getText().toString().trim();
        String subTaskTimer = mSubTaskTimer.getText().toString().trim();
        String timerInSecs ;

        int timerVal = Integer.parseInt(subTaskTimer);

        if (mSpinnerVal == TaskContract.TaskEntry.TIMER_MINS) {
            timerInSecs = Integer.toString(timerVal * 60);
            mTimerUnit = "Mins";
        }else{
            mTimerUnit = "Secs";
            timerInSecs = subTaskTimer;
        }

        Log.e("LOGTAG","Unit -"+mTimerUnit);


        ContentValues subtaskvalues = new ContentValues();

        subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_TASK_ID,mainTaskId);
        subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_TASK_NAME,mainTaskName);
        subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME,subTaskName);
        subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_TIMER,subTaskTimer);
        subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT, mTimerUnit);
        subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_TIMER_SECS,timerInSecs);

        if (mCurrentUri==null){

            Uri newUri = getContentResolver().insert(TaskContract.SubTaskEntry.CONTENT_URI,subtaskvalues);


            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error while creating SubTask",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "SubTask Created Successfully",
                        Toast.LENGTH_SHORT).show();
            }

        }else{

            subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_TASK_ID,taskId);
            subtaskvalues.put(TaskContract.SubTaskEntry.COLUMN_TASK_NAME,taskName);

            Log.e("LOGTAG","Unit -"+mTimerUnit);

            Log.e("LOGTAG","Inside save task update logic "+mCurrentUri);
            Log.e("LOGTAG","SubTaskValues "+subtaskvalues);
            // Otherwise this is an EXISTING Activity, so update the activity with content URI: mCurrentTaskUri
            int rowsAffected = getContentResolver().update(mCurrentUri, subtaskvalues, null, null);



            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_act_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_act_successful),
                        Toast.LENGTH_SHORT).show();
            }


        }




    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {TaskContract.SubTaskEntry._ID,
                TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME,
                TaskContract.SubTaskEntry.COLUMN_TIMER,
                TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT,
        TaskContract.SubTaskEntry.COLUMN_TASK_NAME,
        TaskContract.SubTaskEntry.COLUMN_TASK_ID};

        Log.e("LOGTAG","Inside OnCreate Loader for Subtask loader - return the cursor data");

        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(@NonNull  Loader<Cursor> loader, Cursor data) {


            if (data.moveToNext()) {
                int subtasknameIndex = data.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME);
                int subtasktimerIndex = data.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER);
                int timerunitIndex = data.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT);
                int tasknameIndex = data.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TASK_NAME);
                int taskidIndex = data.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TASK_ID);

                subtaskname=data.getString(subtasknameIndex);
                subtasktimer=data.getString(subtasktimerIndex);
                timerunit = data.getString(timerunitIndex);
                taskId = data.getString(taskidIndex);
                taskName = data.getString(tasknameIndex);


                mSubTaskName.setText(subtaskname);
                mSubTaskTimer.setText(subtasktimer);
                if (timerunit.equals("Mins")){
                    mSpinner.setSelection(0);
                    Log.e("LOGTAG","spinner val min -"+timerunit);
                }else{
                    mSpinner.setSelection(1);
                    Log.e("LOGTAG","spinner val sec -"+timerunit);
                }



            }
    }

    @Override
    public void onLoaderReset(@NonNull  Loader<Cursor> loader) {
        mSubTaskName.setText("");
        mSubTaskTimer.setText("");
        mSpinner.setSelection(1);

    }
}