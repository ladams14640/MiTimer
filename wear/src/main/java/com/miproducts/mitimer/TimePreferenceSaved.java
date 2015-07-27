package com.miproducts.mitimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Use this class to save the time stamp on the last alarm.
 *
 * Created by larry on 7/22/15.
 */
public class TimePreferenceSaved {
    // keep track of the alarm time
    public static final String KEY_ALARM_TIME = "KEY_ALARM_TIME";
    // keep track if alarm was playing
    public static final String KEY_ALARM_PLAYING = "KEY_ALARM_PLAYING";
    // keep track of the last System time before we ended the thread or exited the app
    public static final String KEY_ALARM_SYSTEM_TIME = "KEY_ALARM_SYSTEM_TIME";

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private Context mContext;

    TimePreferenceSaved(Context context){
        this.mContext = context;
        mPrefs = mContext.getSharedPreferences("PREFERENCES", 0);
        mEditor = mPrefs.edit();
    }
    // save the alarm time, called when alarm is made. Or when it is done.
    protected void saveAlarmTime(long timeForAlarm){
        log("saved alarm with value of "+ timeForAlarm);
        mEditor.putLong(KEY_ALARM_TIME, timeForAlarm);
        applyEditorAndCommit();

    }

    protected long getAlarmTime(){
        log("gotAlarmTime with time of " + mPrefs.getLong(KEY_ALARM_TIME, 0));
        return mPrefs.getLong(KEY_ALARM_TIME, 0);
    }

    protected long computeTimeLeftOnAlarm(long currentTime){
        long alarmTime = mPrefs.getLong(KEY_ALARM_TIME, 0);
        log("computeTime with alarmTime" + alarmTime);
        Log.d("TimePreference", "AlarmTime pulled out is " + alarmTime);
        //alarmTime = alarmTime - currentTime;
        if(alarmTime <= 0){
            // might as well save it as such.
            saveAlarmTime(0);
            return 0;
        }else {
            return alarmTime;
        }
    }

    // grab last time if we were paused.
    protected boolean wasPlaying(){
        return mPrefs.getBoolean(KEY_ALARM_PLAYING, false);
    }

    // set whether we were paused.
    protected void setPlaying(boolean isPlaying){
        mEditor.putBoolean(KEY_ALARM_PLAYING, isPlaying);
        applyEditorAndCommit();

    }

    /* Save last System Time */
    protected void saveLastSystemTime(long currentSystemTime){
        mEditor.putLong(KEY_ALARM_SYSTEM_TIME, currentSystemTime);
        applyEditorAndCommit();
    }

    /**
     * If nothing return 0 otherwise return the last SystemTime when we exited.
     * @return
     */
    protected long getLastSystemTime(){
        return mPrefs.getLong(KEY_ALARM_SYSTEM_TIME, 0);
    }
    private void applyEditorAndCommit(){
        mEditor.apply();
        mEditor.commit();
    }

    private void log(String s) {
        Log.d("TimePreferenceSave", s);
    }


}
