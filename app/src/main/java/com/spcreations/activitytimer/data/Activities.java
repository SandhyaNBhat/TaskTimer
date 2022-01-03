package com.spcreations.activitytimer.data;

public class Activities {
    private String mtaskId;
    private String mtaskName;
    private String msubtaskName;
    private String mtimer;
    private String mtimerUnit;
    private String mtimerInSecs;

  public Activities(String taskId, String taskName, String subtaskName, String timer, String timerUnit, String timerInSecs) {
      mtaskId = taskId;
      mtaskName= taskName;
      msubtaskName = subtaskName;
      mtimer = timer;
      mtimerUnit= timerUnit;
      mtimerInSecs = timerInSecs;
    }
    public String getTaskId() {
        return mtaskId;
    }

    public void setTaskId(String taskId) {
       mtaskId = taskId;
    }

    public String getTaskName() {
        return mtaskName;
    }

    public void setTaskName(String taskName) {
       mtaskName = taskName;
    }

    public String getSubtaskName() {
        return msubtaskName;
    }

    public void setSubtaskName(String subtaskName) {
        msubtaskName = subtaskName;
    }

    public String getTimer() {
        return mtimer;
    }

    public void setTimer(String timer) {
      mtimer = timer;
    }

    public String getTimerUnit() {
      return  mtimerUnit;
    }

    public void setTimerUnit(String timerUnit) {
       mtimerUnit = timerUnit;
    }

    public String getTimerInSecs() {
        return mtimerInSecs;
    }

    public void setTimerInSecs(String timerInSecs) {
        mtimerInSecs = timerInSecs;
    }

    @Override
    public String toString() {
        return "Activities{" +
                "mtaskId='" + mtaskId + '\'' +
                ", mtaskName='" + mtaskName + '\'' +
                ", msubtaskName='" + msubtaskName + '\'' +
                ", mtimer='" + mtimer + '\'' +
                ", mtimerUnit='" + mtimerUnit + '\'' +
                ", mtimerInSecs='" + mtimerInSecs + '\'' +
                '}';
    }
}
