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

    public static final String KEY_ALARM_PLAYING = "KEY_ALARM_PLAYING";

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
        mEditor.apply();
        mEditor.commit();
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
        mEditor.apply();
        mEditor.commit();
    }




    private void log(String s) {
        Log.d("TimePreferenceSave", s);
    }


}
