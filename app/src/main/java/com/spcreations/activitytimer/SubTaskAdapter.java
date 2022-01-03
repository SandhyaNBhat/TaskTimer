package com.spcreations.activitytimer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.spcreations.activitytimer.data.TaskContract;

public class SubTaskAdapter extends CursorAdapter {

    public SubTaskAdapter(Context context, Cursor c, int flags) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.subtask_list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView SubtaskName = view.findViewById(R.id.subtask_name);
        TextView SubtaskTimer = view.findViewById(R.id.subtask_timer);
        TextView SubtaskTimerUnit = view.findViewById(R.id.subtask_timer_unit);

        int nameColumnIndex = cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_SUBTASK_NAME);
        int timerColumnIndex = cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER);
        int unitColumnIndex = cursor.getColumnIndex(TaskContract.SubTaskEntry.COLUMN_TIMER_UNIT);

        String subTaskName = cursor.getString(nameColumnIndex);
        String subTaskTimer = cursor.getString(timerColumnIndex);
        String subTaskTimerUnit = cursor.getString(unitColumnIndex);

        SubtaskName.setText(subTaskName);
        SubtaskTimer.setText(subTaskTimer);
        SubtaskTimerUnit.setText(subTaskTimerUnit);
    }
}
