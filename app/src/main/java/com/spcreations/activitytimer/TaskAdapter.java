package com.spcreations.activitytimer;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.spcreations.activitytimer.data.TaskContract;

public class TaskAdapter extends CursorAdapter {

    public ListView mListView;

    protected static class RowViewHolder {
        public ImageView mEditbtn;
        public ImageView mPlaybtn;

    }


   public TaskAdapter(Context context, Cursor c, int flags) {
        super(context, c, 0);

    }






    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
 return LayoutInflater.from(context).inflate(R.layout.list_items,viewGroup,false);
    }

    private View.OnClickListener mOnEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        // final int position = mListView.getPositionForView((View) v.getParent());
            Log.e("LOGTAG", "Title clicked, row %d");
        }
    };

    private View.OnClickListener mOnPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

       //   final int position = mListView.getPositionForView((View) v.getParent());
            Log.e("LOGTAG", "Text clicked, row %d");
        }
    };

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView taskName = view.findViewById(R.id.tname);

        int nameColumnIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME);

        String  mainTaskName = cursor.getString(nameColumnIndex);

        taskName.setText(mainTaskName);

    }
}
