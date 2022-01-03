package com.spcreations.activitytimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spcreations.activitytimer.data.TaskContract;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static android.os.Environment.getExternalStorageDirectory;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.spcreations.activitytimer.data.TaskProvider.LOG_TAG;
import static java.lang.Integer.parseInt;

public class AddTasksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's gender */
    private Spinner mValueSpinner;
    private int mValue = 1;
    private EditText mMainTaskName,mMainTaskTimer;
    private Uri mCurrentTaskUri;
    private View subTaskHeaderLayout,subTaskDetailsLayout,subTaskTimerLayout,subTaskListLayout,layoutSubTaskList;

    private Button mSaveSubTask,mAddSubTask;
    private FloatingActionButton fab_st;
    private TextView mTaskStatus,mSubTaskCnt,mTotalTimer;
    String taskId;
    String taskname,subtaskpresence,tasktimer,timerSecs,activityId,flag;
    String mFileName;
    private ListView subTaskList;
    SubTaskAdapter mSubTaskAdapter;
    ActivityAdapter mActivityAdapter;

    private int timerInSecs,timerValue;

    MediaPlayer mp = new MediaPlayer();

    private static final int EXISTING_TASK_LOADER = 0;
    private static final int SUBTASK_TASK_LOADER = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tasks);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // mAddSubTask = findViewById(R.id.btn_configure_task);
        fab_st = findViewById(R.id.fab_subactivity);
        mTaskStatus = findViewById(R.id.tv_status);
        subTaskTimerLayout = findViewById(R.id.TasksLayout2);
        mMainTaskName = findViewById(R.id.edit_taskname);
        subTaskDetailsLayout = findViewById(R.id.TasksLayout1);
        mValueSpinner = findViewById(R.id.spinner_values);
        subTaskList = findViewById(R.id.subtasklist);
        layoutSubTaskList = findViewById(R.id.LayoutSubTaskList);
        mSubTaskCnt = findViewById(R.id.title_subtasks);
        mTotalTimer = findViewById(R.id.total_timer);


       // mAddSubTask.setVisibility(GONE);
        fab_st.setVisibility(GONE);
        layoutSubTaskList.setVisibility(GONE);

        timerValue=0;


        Intent i = getIntent();
        mCurrentTaskUri = i.getData();


        Log.e("LOGTAG","Current URI "+mCurrentTaskUri);



        if (mCurrentTaskUri == null) {
            setTitle("Add Activity");

        } else {
            Log.e("LOGTAG","Inside Edit Mode of Add Activities Screen");
            setTitle("Edit Activity");
            LoaderManager.getInstance(this).initLoader(EXISTING_TASK_LOADER, null, this);




             mTaskStatus.setVisibility(GONE);
             mValueSpinner.setEnabled(false);
             mMainTaskName.setEnabled(false);

            fab_st.setVisibility(VISIBLE);

            fab_st.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(AddTasksActivity.this, AddSubTasksActivity.class);
                    i.putExtra("task_name", taskname);
                    i.putExtra("task_id", taskId);
                    startActivity(i);

                }
            });

              /*Fetch the activity Id*/
            String[] projection = {TaskContract.TaskEntry._ID,
                    TaskContract.TaskEntry.COLUMN_TASK_NAME,
                    TaskContract.TaskEntry.COLUMN_SUBTASK};

            String selection = TaskContract.TaskEntry._ID + "=?";

            String[] selectionArgs = {Long.toString(ContentUris.parseId(mCurrentTaskUri))};

            Cursor cursor = getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,projection,selection,selectionArgs,null);


            try{
                while(cursor.moveToNext()) {
                    activityId = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry._ID));
                    flag=cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_SUBTASK));
                }
            }finally {
                cursor.close();
            }

           //Show the list of SubActivities for the given MainActivity

            if (flag.equals("Yes")) {
                layoutSubTaskList.setVisibility(VISIBLE);
            }
            Log.e("LOGTAG","Calling loader Manager for Subtask List");
          LoaderManager.getInstance(this).initLoader(SUBTASK_TASK_LOADER,null,this);

            //Populate the subtask listview
          ListView subtaskListView = findViewById(R.id.subtasklist);
          mSubTaskAdapter = new SubTaskAdapter(this,null,0);
          subtaskListView.setAdapter(mSubTaskAdapter);

          subtaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {



                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AddTasksActivity.this);
                builder.setMessage(R.string.edit_or_delete_task);
                builder.setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                  Intent intent = new Intent(AddTasksActivity.this,AddSubTasksActivity.class);

                        Uri uri = ContentUris.withAppendedId(TaskContract.SubTaskEntry.CONTENT_URI,l);
                        //Toast.makeText(TaskListActivity.this, "uri "+uri, Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG,"Uri -"+uri);
                      intent.setData(uri);
                       startActivity(intent);
                    }
                });
                builder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Delete" button, so delete the sub-activity

                        Uri mSubTaskUri = ContentUris.withAppendedId(TaskContract.SubTaskEntry.CONTENT_URI,l);
                        int rowsDeleted = getContentResolver().delete(mSubTaskUri, null, null);

                        // Show a toast message depending on whether or not the delete was successful.
                        if (rowsDeleted == 0) {
                            // If no rows were deleted, then there was an error with the delete.
                            Toast.makeText(AddTasksActivity.this, "Error ", Toast.LENGTH_SHORT).show();
                        } else {
                            // Otherwise, the delete was successful and we can display a toast.
                            Toast.makeText(AddTasksActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

              }
          });

        }

        setupSpinner();

  }




    /**
     * Setup the dropdown spinner that allows the user to select "Yes", "No" values
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter valuesSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_values_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        valuesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);


        // Apply the adapter to the spinner
        mValueSpinner.setAdapter(valuesSpinnerAdapter);

        mMainTaskName = findViewById(R.id.edit_taskname);
        mMainTaskTimer = findViewById(R.id.edit_tasktimer);

        // Set the integer mValue to the constant values
        mValueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                  if (mCurrentTaskUri ==null) {

                      String selection = (String) parent.getItemAtPosition(position);
                      if (!TextUtils.isEmpty(selection)) {
                          if (selection.equals(getString(R.string.value_yes))) {
                              mValue = TaskContract.TaskEntry.SUBTASK_YES;
                              mTaskStatus.setText("Save the activity by clicking ✓ in the tool bar. Click on this activity in the Activities List screen and open in edit mode to configure subactivities.");
                              subTaskTimerLayout.setVisibility(GONE);


                          } else {
                              mValue = TaskContract.TaskEntry.SUBTASK_NO;
                              subTaskTimerLayout.setVisibility(VISIBLE);
                              mTaskStatus.setText("If the activity doesn't have any subactivities, then enter the name and timer value.Save the activity by clicking ✓ in the tool bar.");


                          }
                      }
                  }else {

                      if (subtaskpresence.equals(getString(R.string.value_yes))){

                          mValue = TaskContract.TaskEntry.SUBTASK_YES;
                          subTaskTimerLayout.setVisibility(GONE);


                      }else{

                          mValue = TaskContract.TaskEntry.SUBTASK_NO;
                          subTaskTimerLayout.setVisibility(VISIBLE);
                        //  mAddSubTask.setVisibility(GONE);
                          fab_st.setVisibility(GONE);
                      }
                  }




            }




            // Because AdapterView is an abstract class, onNothingSelected must be defined
          @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mValue = TaskContract.TaskEntry.SUBTASK_YES;
              mAddSubTask.setVisibility(VISIBLE);
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
                saveMainTask();
                // Exit activity
                Log.e("LOGTAG","Exiting AddTasksActivity");
                finish();
                Log.e("LOGTAG","After finish call");
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
              //  showDeleteConfirmationDialog();
                showDeleteConfirmationDialog();

                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the task hasn't changed, continue with navigating up to parent activity
                // which is the {@link TaskListActivity}.
             /*   if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);*/
                NavUtils.navigateUpFromSameTask(AddTasksActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /*
     *Get user input from the Add Task editor and save the task details in database
     */

    private void saveMainTask(){

        String mainTaskName = mMainTaskName.getText().toString().trim();
        String mainTaskTimer = mMainTaskTimer.getText().toString().trim();
        String mainTaskSubtask;


        // Check if this is supposed to be a new activity
        // and check if all the fields in the editor are blank
        if (mCurrentTaskUri == null &&
                TextUtils.isEmpty(mainTaskName) && TextUtils.isEmpty(mainTaskTimer)){
            // Since no fields were modified, we can return early without creating a new activity.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        if (mValue == TaskContract.TaskEntry.SUBTASK_YES){
             mainTaskSubtask="Yes";
        }else{
            mainTaskSubtask ="No";
        }


        ContentValues values = new ContentValues();

        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME,mainTaskName);
        values.put(TaskContract.TaskEntry.COLUMN_SUBTASK,mainTaskSubtask);
        values.put(TaskContract.TaskEntry.COLUMN_TIME_REMINDER,mainTaskTimer);

        if (mCurrentTaskUri == null){

            Uri newUri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI,values);


            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error - Activity not created.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Activity Created Successfully.",
                        Toast.LENGTH_SHORT).show();

                /*Fetch the activity Id*/
              // String[] projection = {TaskContract.TaskEntry._ID,
                   //  TaskContract.TaskEntry.COLUMN_TASK_NAME};

                //String selection = TaskContract.TaskEntry._ID + "=?";

               // String[] selectionArgs = {Long.toString(ContentUris.parseId(newUri))};
                //Cursor cursor = getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,projection,null,null,null);
             // mActivityAdapter = new ActivityAdapter(this,cursor);
              // Log.e("LOGTAG","Calling swap cursor from save maintask");
            //  mActivityAdapter.swapCursor(cursor);
            }


        }else {

             Log.e("LOGTAG","Inside save task update logic "+mCurrentTaskUri);
            // Otherwise this is an EXISTING Activity, so update the activity with content URI: mCurrentTaskUri
            int rowsAffected = getContentResolver().update(mCurrentTaskUri, values, null, null);



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


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     // @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
   /* private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }*/

    /**
     * Prompt the user to confirm that they want to delete this activity.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the activity.
               deleteActivity();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteActivity() {
        // Only perform the delete if this is an existing activity.
        if (mCurrentTaskUri != null) {
            // Call the ContentResolver to delete the activity at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentTaskUri
            // content URI already identifies the activity that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentTaskUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_activity_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_activity_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        Intent intent = new Intent(AddTasksActivity.this,TaskListActivity.class);
        startActivity(intent);

    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case EXISTING_TASK_LOADER:
                String[] projection = {TaskContract.TaskEntry._ID,
                        TaskContract.TaskEntry.COLUMN_TASK_NAME,
                        TaskContract.TaskEntry.COLUMN_SUBTASK,
                        TaskContract.TaskEntry.COLUMN_TIME_REMINDER};

                return new CursorLoader(this,
                        mCurrentTaskUri,
                        projection,
                        null,
                        null,
                        null);



            case SUBTASK_TASK_LOADER:
                Log.e("LOGTAG","Inside OnCreate Loader for Subtask loader");
                String[] projection1 = {TaskContract.SubTaskEntry._ID,
                        TaskContract.SubTaskEntry.COLUMN_TASK_NAME,
                        TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME,
                        TaskContract.SubTaskEntry.COLUMN_TIMER,
                        TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT,
                        TaskContract.SubTaskEntry.COLUMN_TIMER_SECS,
                        TaskContract.SubTaskEntry.COLUMN_TASK_ID};

                String selection = TaskContract.SubTaskEntry.COLUMN_TASK_ID + "=?";

                String[] selectionArgs = {activityId};
                Log.e("LOGTAG","Inside OnCreate Loader for Subtask loader - return the cursor data");

                return new CursorLoader(this,
                        TaskContract.SubTaskEntry.CONTENT_URI,
                        projection1,
                        selection,
                        selectionArgs,
                        null);


            default: return null;

        }


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.e("LOGTAG","Inside onLoadFinished");

        switch(loader.getId()){
            case EXISTING_TASK_LOADER:

                if (data == null || data.getCount() < 1) {
                    return;
                }

                if (data.moveToFirst()){

                    int nameColumnIndex = data.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME);
                    int spinnerColumnIndex = data.getColumnIndex(TaskContract.TaskEntry.COLUMN_SUBTASK);
                    int timerColumnIndex = data.getColumnIndex(TaskContract.TaskEntry.COLUMN_TIME_REMINDER);
                    int taskIdColumnIndex = data.getColumnIndex(TaskContract.TaskEntry._ID);


                    taskname = data.getString(nameColumnIndex);
                    taskId=data.getString(taskIdColumnIndex);
                    subtaskpresence = data.getString(spinnerColumnIndex);
                    tasktimer = data.getString(timerColumnIndex);

                    if (subtaskpresence.equals("Yes")){
                        mValueSpinner.setSelection(0);
                    }else{
                        mValueSpinner.setSelection(1);
                    }

                    mMainTaskName = findViewById(R.id.edit_taskname);
                    mMainTaskTimer = findViewById(R.id.edit_tasktimer);
                    mValueSpinner = findViewById(R.id.spinner_values);


                    Log.e("LOGTAG","taskname -"+taskname);

                    mMainTaskName.setText(taskname);
                    mMainTaskTimer.setText(tasktimer);

                }
                break;
            case SUBTASK_TASK_LOADER:


             try {
                 timerValue=0;
                 while (data.moveToNext()) {
                     int subtasktimerColumnIndex = data.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER_SECS);


                     timerSecs = data.getString(subtasktimerColumnIndex);

                     timerInSecs = parseInt(timerSecs);

                     timerValue = timerValue+timerInSecs;


                     Log.e("LOGTAG", "Timer Value of " + timerInSecs);


                 }}finally{
                 long hour = (Long.valueOf(timerValue) / 3600) % 24;
                 long min = (Long.valueOf(timerValue) / 60) % 60;
                 long sec = (Long.valueOf(timerValue) / 1) % 60;
                 mSubTaskCnt.setText("Total Subactivities: "+ data.getCount());
                 mTotalTimer.setText("Total Time : "+hour+"hour"+" : "+min+"mins"+" : "+sec+"secs");
                 mSubTaskAdapter.swapCursor(data);

                 }

             Log.e("LOGTAG","Timer "+timerValue);


                Log.e("LOGTAG","Adapter is set properly");
                break;
        }



    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader) {

        switch (loader.getId()){
            case EXISTING_TASK_LOADER:
                mMainTaskTimer.setText("");
                mMainTaskName.setText("");
                mValueSpinner.setSelection(1);
                break;
            case SUBTASK_TASK_LOADER:

                mSubTaskAdapter.swapCursor(null);
                //mSpinner.setSelection(1);
               break;

        }



    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddTasksActivity.this,TaskListActivity.class);
        startActivity(intent);
    }


}