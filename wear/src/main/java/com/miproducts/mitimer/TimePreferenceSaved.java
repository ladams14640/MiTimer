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
    public static final String KEY_ALARM_TIME = "KEY_ALARM_TIME";

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
        Log.d("TimePreference", "saved alarm");
        mEditor.putLong(KEY_ALARM_TIME, timeForAlarm);
        mEditor.apply(); 
        mEditor.commit();
    }

    // grab the last alarm,
    protected long getAlarmTime(){
        return mPrefs.getLong(KEY_ALARM_TIME, 0);
    }
    // TODO have not tested yet, we want to check the alarm when we start activity.
    // TODO if there is time left set the time according.
    protected long computeTimeLeftOnAlarm(long currentTime){

        long alarmTime = mPrefs.getLong(KEY_ALARM_TIME, 0);
        Log.d("TimePreference", "AlarmTime pulled out is " + alarmTime);
        alarmTime-=currentTime;
        if(alarmTime <= 0){
            // might as well save it as such.
            saveAlarmTime(0);
            return 0;
        }else {
            return alarmTime;
        }
    }


}
