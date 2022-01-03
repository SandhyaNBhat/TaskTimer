package com.spcreations.activitytimer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spcreations.activitytimer.data.TaskContract;

import static android.view.View.GONE;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityHolder> {

    private Context mContext;
    private Cursor mCursor;
    private Boolean mIsExpandable;
    private onItemClickListener mListener;
    private long mTaskId;

   public  ActivityAdapter(Context context, Cursor cursor){
       mContext = context;
       mCursor = cursor;

   }

   public interface onItemClickListener {
       void onEditClick(int position,long l);
       void onStartClick (int position,long l);

   }

   public void setOnItemClickListener(onItemClickListener listener){
       mListener = listener;
   }

    public class ActivityHolder extends RecyclerView.ViewHolder{
       public TextView activityName;
       public TextView activityId;
       public Button editActivity;
       public Button playActivity;

       RelativeLayout activityLayout;
       LinearLayout buttonsLayout;




        public ActivityHolder(View itemView, final onItemClickListener listener) {
            super(itemView);

            activityName = itemView.findViewById(R.id.tname);
            activityId= itemView.findViewById(R.id.taskId);
            editActivity = itemView.findViewById(R.id.btn_edit);
            playActivity = itemView.findViewById(R.id.btn_run);

            activityLayout =itemView.findViewById(R.id.activityLayout);
            buttonsLayout = itemView.findViewById(R.id.buttonsLayout);

            buttonsLayout.setVisibility(GONE);
            mIsExpandable = false;

            activityLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("LOGTAG","Clicked on the list item");
                    mIsExpandable=!mIsExpandable;
                    buttonsLayout.setVisibility(mIsExpandable? View.VISIBLE: View.GONE);

                }
            });

           editActivity.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(listener!=null){
                       int position = getAbsoluteAdapterPosition();
                       String Id = (String) activityId.getText();
                       Long l = Long.valueOf(Id);

                       if(position!=RecyclerView.NO_POSITION){
                           listener.onEditClick(position,l);
                       }
                   }

               }
           });

           playActivity.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if(listener!=null){
                       int position = getAbsoluteAdapterPosition();
                       String Id = (String) activityId.getText();
                       Long l = Long.valueOf(Id);

                       if(position!=RecyclerView.NO_POSITION){
                           listener.onStartClick(position,l);
                       }
                   }
               }
           });

        }


    }

    @Override
    public ActivityHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View  view = inflater.inflate(R.layout.activity_items,parent,false);
        return new ActivityHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(ActivityAdapter.ActivityHolder holder, int position) {
       if(!mCursor.moveToPosition(position)){
           return;
       }

        String  mainTaskName = mCursor.getString(mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
        String mMainTaskId = mCursor.getString(mCursor.getColumnIndex(TaskContract.TaskEntry._ID));
      //  mTaskId = mCursor.getPosition().getId();

        holder.activityName.setText(mainTaskName);
        holder.activityId.setText(mMainTaskId);

        holder.buttonsLayout.setVisibility(mIsExpandable? View.VISIBLE: View.GONE);

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();

    }



    public void swapCursor(Cursor newCursor){

       Log.e("LOGTAG","Inside swap cursor");
       if(mCursor !=null){
           mCursor.close();
       }

       mCursor = newCursor;

       if(newCursor !=null){
           notifyDataSetChanged();
       }
    }


}
