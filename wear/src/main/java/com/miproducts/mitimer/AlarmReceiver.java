package com.miproducts.mitimer;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.util.SparseArray;


public class AlarmReceiver extends BroadcastReceiver
{

    private TimePreferenceSaved mPrefSaved;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent service1 = new Intent(context, MainActivity.class);
        context.startService(service1);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        wl.acquire(3000);
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(3000);
        mPrefSaved = new TimePreferenceSaved(context);
        mPrefSaved.saveAlarmTime(0);
        Log.d("AlarmReceiver", "onReceived");
    }
}