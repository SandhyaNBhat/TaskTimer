package com.spcreations.activitytimer;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spcreations.activitytimer.data.Activities;
import com.spcreations.activitytimer.data.TaskContract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.view.View.GONE;


public class StartActivity extends AppCompatActivity {

    String taskname,taskId,subtaskpresence,mTaskName;
    Boolean subActivityFlag,errorFlag,stopSpeak;

    TextView strtmsg,currenttask,progress;
    FloatingActionButton strtBtn;


    Thread sat;
    TextToSpeech mTTS;

    private int currentProgress = 0;
    private int maxProgress;
    private ProgressBar progressBar;

    private final AtomicBoolean running = new AtomicBoolean(false);

    CountDownTimer mCountDownTimer;
    Boolean mTimerRunning,mStrtPause,mCompletion,mSpeaking;
    long mTimerLeft,mTimeLeftInMilisecs,mTotaltimerVal,mThreadSleepTime;
    int mStrtIndex,mIndex;

    ArrayList<Activities> activityList = new ArrayList<Activities>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        strtmsg = findViewById(R.id.activitystrtmsg);
        currenttask = findViewById(R.id.currentactivity);
        strtBtn = findViewById(R.id.im_start);
        strtmsg.setText("Your activity has been started");
        progressBar = findViewById(R.id.progressbar);
        progress = findViewById(R.id.progress);

        Intent intent = getIntent();
        taskname = intent.getStringExtra("task_name");
        taskId = intent.getStringExtra("task_id");

        subActivityFlag = getActivityDetails(taskname);
        mStrtPause = false;
        mStrtIndex = 0;
        mTimeLeftInMilisecs = 0;
        mCompletion = true;

        //Invoke the runTimer functionality to run the activity.

        Log.e("LOGTAG","Calling the runTimer functionality");

        if (subActivityFlag) {
            stopSpeak=false;
            runTimer(mStrtIndex, mTimeLeftInMilisecs, mStrtPause);
        }

      //Implement the Start/Pause button click functionality
        strtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mStrtPause = true;

                if (mTimerRunning) {
                    pauseTimer();

                }
                else{
                    startTimer();

                }

            }
        });




    }

    public void pauseTimer(){

        Log.e("LOGTAG","Inside pause timer");
        stopSpeak=true;

        strtBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play));
        mCompletion = false;

        Log.e("LOGTAG","Timer cancelled");
        sat.interrupt();
        mCountDownTimer.cancel();

        Log.e("LOGTAG","Thread interrupted");

        mTimerRunning = false;

        Log.e("LOGTAG","Running flag set to false");

    }

    public void startTimer(){
        stopSpeak=false;

        strtBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause));
        runTimer(mIndex,mTimeLeftInMilisecs,mStrtPause);


    }

    /*
    getActivityDetails will fetch the task details from tasks and subtasks table. Fetched data is stored in arraylist
     */


    public Boolean getActivityDetails(String taskName){

        Log.e("LOGTAG","Getting the activity details");
        errorFlag = false;

        String taskId;
        String task;
        String subTaskName;
        String timer;
        String timerUnit;
        String timerinSecs;
        String subTaskPresence;


        //Get the details from Subtasks table for the given task

        Log.e("LOGTAG","Fetching the subtask information");

        String[] projection = {TaskContract.SubTaskEntry.COLUMN_TASK_ID,
                TaskContract.SubTaskEntry.COLUMN_TASK_NAME,
                TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME,
                TaskContract.SubTaskEntry.COLUMN_TIMER,
                TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT,
                TaskContract.SubTaskEntry.COLUMN_TIMER_SECS};

        String selection = TaskContract.SubTaskEntry.COLUMN_TASK_NAME + "=?";


        String[] selectionArgs = {taskname};

        Cursor cursor = getContentResolver().query(TaskContract.SubTaskEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,null);

        if (cursor.getCount()>0){
            Log.e("LOGTAG","Looping through the cursor to get the subtask details");
            try{
                while (cursor.moveToNext()){
                     taskId = cursor.getString(cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TASK_ID));
                     task= cursor.getString(cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TASK_NAME));
                     subTaskName=cursor.getString(cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME));
                     timer=cursor.getString(cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER));
                     timerUnit=cursor.getString(cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT));
                     timerinSecs=cursor.getString(cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER_SECS));


                    activityList.add(new Activities(taskId,task,subTaskName,timer,timerUnit,timerinSecs));

                }
            }finally {
                Log.e("LOGTAG","Cursor close");
                cursor.close();

            }
        }else{

            Log.e("LOGTAG","No subtasks for the given task id");
            Log.e("LOGTAG","Fetch the task timer from tasks table");

            String[] project = {TaskContract.TaskEntry._ID,
                    TaskContract.TaskEntry.COLUMN_TASK_NAME,
                    TaskContract.TaskEntry.COLUMN_TIME_REMINDER,
                    TaskContract.TaskEntry.COLUMN_SUBTASK,
                    TaskContract.TaskEntry.COLUMN_SUBTASK};

            String select = TaskContract.TaskEntry.COLUMN_TASK_NAME + "=?";

            String[] selectArgs = {taskname};

            Cursor c1 = getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,project,select,selectArgs,null);
            Log.e("LOGTAG","Data fetched in a cursor");

           try{
               while (c1.moveToNext()){


                   subTaskPresence = c1.getString(c1.getColumnIndex(TaskContract.TaskEntry.COLUMN_SUBTASK));
                   Log.e("LOGTAG","Subtask present ? - "+subTaskPresence);

                   if (subTaskPresence.equals("No")) {
                       Log.e("LOGTAG","Data fetched in a cursor for the task details");
                       taskId = c1.getString(c1.getColumnIndex(TaskContract.TaskEntry._ID));
                       Log.e("LOGTAG","taskId -"+taskId);
                       task= c1.getString(c1.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                       Log.e("LOGTAG","task -"+task);
                       subTaskName="NA";
                       timer=c1.getString(c1.getColumnIndex(TaskContract.TaskEntry.COLUMN_TIME_REMINDER));
                       Log.e("LOGTAG","timer -"+timer);
                       timerUnit="Mins";
                       int timerVal = Integer.parseInt(timer);
                       timerinSecs=Integer.toString(timerVal * 60);
                       Log.e("LOGTAG","timerinSecs -"+timerinSecs);
                       activityList.add(new Activities(taskId, task, subTaskName, timer, timerUnit, timerinSecs));

                   }else{
                       strtmsg.setText("Please configure sub-activities before starting");
                       errorFlag=true;

                   }

               }
           }finally{
                c1.close();
            }
        }
         if(errorFlag){
             return false;
         }else{
             return true;
         }

    }
    /*
    runTimer function creates new thread to run each of the activities and sub-activities associated with it.
     */

    public void runTimer(int y,long mTimerLeft, boolean restrtflag){

        Log.e("LOGTAG","Inside the runTimer functionality");

        Log.e("LOGTAG","Y- "+y+"mTimerLeft "+mTimerLeft+" restrtflag "+restrtflag);


            sat = new Thread() {
                @Override
                public void run() {
                    try {

                        running.set(true);
                        while (running.get()) {



                            for (int i = y; i < activityList.size(); i++) {

                                Log.e("LOGTAG", "Inside the loop");
                                //Fetch activity details from the activityList Arraylist for the given index
                                String mTaskName = activityList.get(i).getSubtaskName();

                                if (mTaskName.equals("NA")) {
                                    mTaskName = activityList.get(i).getTaskName();
                                }
                                String timer = activityList.get(i).getTimer();
                                String timerInSec = activityList.get(i).getTimerInSecs();

                                long timerinmins, timerinsecs;
                                if (timer != null) {
                                    timerinmins = Long.parseLong(timer);
                                    timerinsecs = Long.parseLong(timerInSec);
                                } else {
                                    timerinmins = 1;
                                    timerinsecs = 60;
                                }

                                String currentTask = mTaskName;

                                if(!mCompletion){
                                    mThreadSleepTime=mTimerLeft;

                                }else{
                                    mThreadSleepTime = timerinsecs*1000;
                                }





                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!stopSpeak) {
                                            currenttask.setText("Current-Activity : " + currentTask);
                                        }
                                    }
                                });


                               // if (!Thread.interrupted())
                               if(!stopSpeak) {

                                    Log.e("LOGTAG", "TTS before the start of activity");

                                    mTTS = new TextToSpeech(StartActivity.this, new TextToSpeech.OnInitListener() {
                                        @Override
                                        public void onInit(int status) {

                                            Log.e("LOGTAG", "Status " + status);
                                            if (status == TextToSpeech.SUCCESS) {
                                                Log.e("LOGTAG", "Text to speech " + TextToSpeech.SUCCESS);

                                                int result = mTTS.setLanguage(Locale.UK);

                                                Log.e("LOGTAG", "language is successfully set " + result);

                                                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                                    Log.e("LOGTAG", "Language not supported");
                                                } else {
                                                    Log.e("LOGTAG", "Successfully initialized");
                                                    mSpeaking = true;
                                                    mTTS.speak("Starting  " + currentTask, TextToSpeech.QUEUE_FLUSH, null, "1");

                                                }

                                                mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                                    @Override
                                                    public void onStart(String s) {
                                                        Log.e("LOGTAG","TTS Started"+running.get());
                                                        mSpeaking = true;

                                                    }

                                                    @Override
                                                    public void onDone(String utteranceId) {
                                                        Log.e("LOGTAG", "TTS finished"+running.get());
                                                        mSpeaking = false;
                                                    }

                                                    @Override
                                                    public void onError(String s) {
                                                        Log.e("LOGTAG", "Error in TTS ");

                                                    }
                                                });
                                            } else {
                                                Log.e("LOGTAG", "Issue with initialization " + TextToSpeech.SUCCESS);
                                            }

                                        }
                                    });
                                }

                              do{

                                  Log.e("LOGTAG","TTS Speaking "+running.get());
                                  Thread.sleep(1000);
                                  Log.e("LOGTAG","After sleeping "+running.get());

                              }while(mSpeaking);

                                //check if running flag is true before proceeding to invoke the countdown timer
                                if (running.get()) {
                                    try {
                                        int finalI = i;
                                        Log.e("LOGTAG", "value of index " + finalI);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (!mCompletion){
                                                    mTimeLeftInMilisecs = mTimerLeft;
                                                    mTotaltimerVal = mTimerLeft;

                                                } else {
                                                    mTimeLeftInMilisecs = timerinsecs * 1000;
                                                    mTotaltimerVal = timerinsecs;
                                                }






                                                Log.e("LOGTAG", "Timer value " + mTimeLeftInMilisecs);

                                                  mCountDownTimer = new CountDownTimer(mTimeLeftInMilisecs, 1000) {


                                                        @Override
                                                        public void onTick(long time) {

                                                            mTimerRunning = true;
                                                            mTimeLeftInMilisecs = time;
                                                            mIndex = finalI;

                                                            updateTimerStats(mTimeLeftInMilisecs, timerinsecs);


                                                        }

                                                        @Override
                                                        public void onFinish() {
                                                            // mTaskStatus.setText("Task " + subTaskName + "Done!");

                                                            mTTS = new TextToSpeech(StartActivity.this, new TextToSpeech.OnInitListener() {
                                                                @Override
                                                                public void onInit(int status) {

                                                                    Log.e("LOGTAG", "Status " + status);
                                                                    if (status == TextToSpeech.SUCCESS) {
                                                                        Log.e("LOGTAG", "Text to speech " + TextToSpeech.SUCCESS);

                                                                        int result = mTTS.setLanguage(Locale.UK);

                                                                        Log.e("LOGTAG", "language is successfully set " + result);

                                                                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                                                            Log.e("LOGTAG", "Language not supported");
                                                                        } else {
                                                                            Log.e("LOGTAG", "Successfully initialized");
                                                                            mTTS.speak(currentTask + "  " + " Completed", TextToSpeech.QUEUE_FLUSH, null, null);
                                                                        }
                                                                    } else {
                                                                        Log.e("LOGTAG", "Issue with initialization " + TextToSpeech.SUCCESS);
                                                                    }

                                                                }
                                                            });

                                                            currentProgress = 0;
                                                            mCompletion = true;

                                                        }
                                                    }.start();
                                                }

                                        });
                                            //Thread.sleep(timerinsecs * 1000);
                                        Thread.sleep(mThreadSleepTime);
                                        Thread.sleep(3000);
                                    } catch (Exception e) {
                                        Log.e("LOGTAG", "Exception occurred " + e);
                                        e.printStackTrace();
                                        running.set(false);
                                        mCompletion = false;
                                        Thread.currentThread().interrupt();
                                    }
                                }//2nd time running flag check before invoking the countdown timer.


                            }//for loop for looping through the sub-activities.

                            if (running.get()) {
                               /* mp = MediaPlayer.create(StartActivity.this, R.raw.finalmessage);
                                mp.start();*/


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        strtmsg.setText("Congratulations!! Your activity is completed.");
                                        currenttask.setVisibility(GONE);
                                    }
                                });

                                mTTS = new TextToSpeech(StartActivity.this, new TextToSpeech.OnInitListener() {
                                    @Override
                                    public void onInit(int status) {

                                        Log.e("LOGTAG", "Status " + status);
                                        if (status == TextToSpeech.SUCCESS) {
                                            Log.e("LOGTAG", "Text to speech " + TextToSpeech.SUCCESS);

                                            int result = mTTS.setLanguage(Locale.UK);

                                            Log.e("LOGTAG", "language is successfully set " + result);

                                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                                Log.e("LOGTAG", "Language not supported");
                                            } else {
                                                Log.e("LOGTAG", "Successfully initialized");
                                                mTTS.speak("Congratulations!! Your activity is completed.", TextToSpeech.QUEUE_FLUSH, null, null);
                                            }
                                        } else {
                                            Log.e("LOGTAG", "Issue with initialization " + TextToSpeech.SUCCESS);
                                        }

                                    }
                                });


                            }
                            running.set(false);


                        }//first time running flag check

                    } catch (Exception e) {
                        Log.e("LOGTAG", "Exception caught  " + e);
                        e.printStackTrace();
                        running.set(false);
                        //Thread.currentThread().interrupt();
                    }//Run block end
                }

            } ;//Thread end



        sat.start();

    }

    private void updateTimerStats(long time,long timerinsecs){


        NumberFormat f = new DecimalFormat("00");
        long hour = (time / 3600000) % 24;
        long min = (time / 60000) % 60;
        long sec = (time / 1000) % 60;


        currentProgress = currentProgress +1000;
        progressBar.setProgress(currentProgress,true);
        maxProgress = 1000*(int)timerinsecs;
        progressBar.setMax(maxProgress);

        String progresstext = String.valueOf(min)+":"+String.valueOf(sec);
        progress.setText(progresstext);

    }



    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running.set(false);
        try{
            sat.interrupt();
        }catch(NullPointerException e){
            Log.e("LOGTAG","Thread not present");
        }

        if(mTTS !=null ){
            mTTS.stop();
            mTTS.shutdown();
        }

    }


    @Override
    public void onBackPressed() {
        super.onDestroy();
        running.set(false);
        try{
            sat.interrupt();
        }catch(NullPointerException e){
            Log.e("LOGTAG","Thread not present");
        }

        if(mTTS !=null ){
            mTTS.stop();
            mTTS.shutdown();
        }
        Intent intent = new Intent(StartActivity.this,TaskListActivity.class);
        startActivity(intent);
    }


}