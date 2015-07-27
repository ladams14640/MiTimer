package com.miproducts.mitimer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.miproducts.mitimer.util.Constants;
import com.miproducts.mitimer.util.TimeKeeper;
import com.miproducts.mitimer.util.TimerFormat;

public class MainActivity extends Activity{
    private final String TAG = "MainActivity";
    public static final String ACTION = BuildConfig.APPLICATION_ID + ".action.ALARM";

    private TextView tvColon;
    private FrameLayout container;
    private TimerView mTimerView;

    private DismissOverlay llDismissalOverlay;
    private FrameLayout flLayout;

    // for exiting the app
    public DismissOverlayView mDismissOverlay;


    public Button bStart;
    public Button bReset;

    public BorderTextView btvMinToHr;
    public BorderTextView btvSecsToMin;

    private TextView tvHr;
    private TextView tvMin;

    private int min = 60;
    private int hr = 60;
    private int convertToSecs = 1000;

    private TimePreferenceSaved prefClass;

    private boolean isAlarmSet = false;

    // temp keep track of the ccountDownTime
    // these three will handle the countdown display for the user.
    private long countDownTime;
    int countDownTimeHours;
    private final Handler handler = new Handler();
    private ThreadTimer mTimerThread;

    // keep track of the display time broken down
    private TimeKeeper timeKeeper;

    // tell us to kill thread - from notification

    final BroadcastReceiver brKillThread = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("brKillThread Received Message");
          handler.removeCallbacks(mTimerThread);
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get a copy of the preference handling object and preferences
        prefClass = new TimePreferenceSaved(this);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                container = (FrameLayout) findViewById(R.id.container);
                mTimerView = (TimerView) findViewById(R.id.timerView);
                llDismissalOverlay = (DismissOverlay) findViewById(R.id.llDismissalLayout);
                flLayout = (FrameLayout) findViewById(R.id.flViews);

                initButtons();

                container.setOnTouchListener(new View.OnTouchListener() {

                    // I believe I must handle the onTouch events here, and send them down the chain, because
                    // they don't seem to be picked up deep down, even as far down as TimerView
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                       // lets make sure we dont mess with the time while it's running.

                            // this awakens onInterceptDispatch.
                            // NOT GETTING ActionEvent.ON_MOVE without this
                            mTimerView.onTouchEvent(event);
                            // keep false to keep sending it down the chain and not absorbing.
                            flLayout.onTouchEvent(event);


                        return false;
                    }
                });


                initTextViews();
                // grab old time compare to now and handle according.
                grabCurrentAlarmTime();


            }

        });
    }



    private void initTextViews() {
        btvMinToHr = (BorderTextView) findViewById(R.id.btvMinOrHr);
        btvSecsToMin = (BorderTextView) findViewById(R.id.btvSecsToMin);
        tvHr = (TextView) findViewById(R.id.tvHrTxt);
        tvMin = (TextView) findViewById(R.id.tvMinTxt);
        tvColon = (TextView) findViewById(R.id.colon);
    }

    private void initButtons() {
        bStart = (Button) findViewById(R.id.bStart);
        bReset = (Button) findViewById(R.id.bReset);

        /**
         * 3 SCENARIOS:
            * Scenario #1 - (Start) last saved  alarm in preference is 0:
         *  1.1 isAlarmSet = true;
         *  1.2 grab values from M_TIMER_VIEW
         *  1.3 setup notification Timer & Alarm Timer
         *  1.4 Setup Timer Thread
            * Scenario #2 - (Start) last saved alarm in preference is above 0:
         *  2.1 isAlarmSet = true;
         *  2.2 grab values from PREFS
         *  2.3 setup notification Timer & Alarm Timer
         *  2.4 Setup Timer Thread
            * Scenario #3 - (Pause) we are pausing:
         *  3.1 isAlarm = false;
         *  3.2 save prefs and alarm
         *  3.3 Stop thread
         *  3.4 kill alarm & kill notification.
         */
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Scenario #1 and #2
                if(!isAlarmSet){
                   // log("Start");
                    // either way these need to get done
                    bStart.setText("Pause");
                    isAlarmSet = true;
                    // get the alarmTime - still need to add it to the system time to get the countdown time
                    long alarmTime = prefClass.getAlarmTime();
                    //log("got alarmTime from preference " + alarmTime);

                    // Scenario #2
                    // preference had something saved.
                    if(alarmTime != 0){
                        // get HR, MIN, SEC for display.
                        timeKeeper = TimerFormat.breakDownMilliSeconds(alarmTime);
                        //TODO #ISSUE when we unPause mins and secs become hrs and mins.
                        // there was hrs stored.
                        if(timeKeeper.getHr() != 0) {
                            //log("hr and minute of the timeKeeper is " + (int) timeKeeper.getHr() + " " + (int) timeKeeper.getMin());
                            //TODO was getMin() and getHr() - want to see if i can fixm y shit.
                            setupTimer(true, (int) timeKeeper.getMin(), (int) timeKeeper.getHr());
                            //prefClass.saveAlarmTime(System.currentTimeMillis() + countDownTime);
                        }
                        // there was minutes and seconds stored.
                        else{
                            //log("Minute and Seconds of the timeKeeper is " + (int) timeKeeper.getMin() + " " + (int) timeKeeper.getSec());

                            setupTimer(false, (int) timeKeeper.getSec(), (int) timeKeeper.getMin());
                        }
                    }

                    // preference had nothing saved.
                    else {
                        // setup Alarm
                        setupTimer(true, mTimerView.getSetMinutes(), mTimerView.getSetHours());
                    }

                }
                // Alarm was already set, assume that we just clicked to Pause
                else {
                    //log("Pause");
                    cancelCountdownNotification();
                    prefClass.setPlaying(false);
                    isAlarmSet = false;
                    // thread will save to prefs the remaining time.
                    if(mTimerThread != null)
                        if(mTimerThread.isRunning())
                            mTimerThread.pauseRunning();
                    //handler.removeCallbacks(mTimerThread);
                    mTimerThread = null;
                    //TODO not saving the time, this may cause issues! if we pause then leave - solution above perhaps with immediatel conditional above.
                    bStart.setText("Start");
                    stopAlarm();
                }

            }
        });
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCountdownNotification();


                if (mTimerThread != null) {
                    mTimerThread.cancelRunning();
                    // keeps resetng the time
                    mTimerThread.interrupt();
                }
                handler.removeCallbacks(mTimerThread);
                mTimerThread = null;
                isAlarmSet = false;
                bStart.setText("Start");

                mTimerView.setMinutes(0);
                mTimerView.setHours(0);
                mTimerView.resetArc();

                btvSecsToMin.setText("0");
                btvMinToHr.setText("0");

                // redundant since we do this in thread but we will see
                setMinutes(0);
                setHours(0);
                setHrTitle("HR");
                setMinTitle("MIN");
                resetHourAndMinute();
                resetPrefAlarmTime();
                // end of redundancy
                prefClass.setPlaying(false);
            }
        });
    }

    private void cancelCountdownNotification() {
        //log("CancelCountdownNotificatioj");

        NotificationManager notifyMgr =
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notifyMgr.cancel(Constants.NOTIFICATION_TIMER_COUNTDOWN);

    }
    //TODO #2 dont accept touches on the arc when its countdown time.


    // grab old alarmtime and compare to now
    private void grabCurrentAlarmTime() {
        //log("grabCurrentAlarm");

        // get the alarmTime - still need to add it to the system time to get the countdown time
        long alarmTime = prefClass.getAlarmTime();
        //log("got alarmTime from preference " + alarmTime);

        // Something was saved lets startup.
        if(alarmTime != 0){
          boolean wasPlaying = prefClass.wasPlaying();
            // was playing so we want to continue playing
            // get HR, MIN, SEC for display.
            timeKeeper = TimerFormat.breakDownMilliSeconds(alarmTime);

            if(wasPlaying) {
                isAlarmSet = true;
                bStart.setText("Pause");



                // there was hrs stored.
                if (timeKeeper.getHr() != 0) {
                    //log("hr and minute of the timeKeeper is " + (int) timeKeeper.getHr() + " " + (int) timeKeeper.getMin());

                    setupTimer(true, (int) timeKeeper.getMin(), (int) timeKeeper.getHr());
                    //prefClass.saveAlarmTime(System.currentTimeMillis() + countDownTime);
                }
                // there was minutes and seconds stored.
                else {
                    //log("Minute and Seconds of the timeKeeper is " + (int) timeKeeper.getMin() + " " + (int) timeKeeper.getSec());

                    setupTimer(false, (int) timeKeeper.getSec(), (int) timeKeeper.getMin());
                }
            }
            // was not playing.
            else {
                // there was hrs stored.
                if (timeKeeper.getHr() != 0) {
                    //log("hr and minute of the timeKeeper is " + (int) timeKeeper.getHr() + " " + (int) timeKeeper.getMin());

                   // setupTimer(true, (int) timeKeeper.getMin(), (int) timeKeeper.getHr());
                    btvMinToHr.setText(Integer.toString((int) timeKeeper.getHr()));
                    btvSecsToMin.setText(Integer.toString((int)timeKeeper.getMin()));
                    //prefClass.saveAlarmTime(System.currentTimeMillis() + countDownTime);
                }
                // there was minutes and seconds stored.
                else {
                    log("Minute and Seconds of the timeKeeper is " + (int) timeKeeper.getMin() + " " + (int) timeKeeper.getSec());
                    btvMinToHr.setText(Integer.toString((int) timeKeeper.getMin()));
                    btvSecsToMin.setText(Integer.toString((int) timeKeeper.getSec()));
                   // setupTimer(false, (int) timeKeeper.getSec(), (int) timeKeeper.getMin());
                }
            }
        }
        // nothing saved.
        else {
            isAlarmSet = false;
            bStart.setText("Start");

        }
    }




    @Override
    protected void onDestroy() {
        handler.removeCallbacks(mTimerThread);
        if(mTimerThread != null){
            //TODO testing this seem to have a problem when i come back to the app i never saved the last time.
            saveTimeInPrefs(mTimerThread.getCountDownTime());
            mTimerThread.cancelRunning();
            mTimerThread = null;

        }
        unregisterReceiver(brKillThread);
        super.onDestroy();

    }

    // Took this from Timer sample project
    // timer stuff all came from Android Timer project sample

    /**
     *
     * @param isHourSet - if there was hours
     * @param durSecs
     * @param durMins
     */
    private void setupTimer(boolean isHourSet, long durSecs, long durMins) {

        long fullTime = 0;
        //log("duration =" + durSecs);
        if(isHourSet){
            long durationMillisSecs = durSecs * convertToSecs * min;
            long durationMillisMin = durMins * convertToSecs * min * hr;
            fullTime = durationMillisSecs + durationMillisMin;
        }else {
            long durationMillisSecs = durSecs * convertToSecs;
            long durationMillisMin = durMins * convertToSecs * min;
            fullTime = durationMillisSecs + durationMillisMin;

        }

        // TODO might have to delete till we iron out shit.
        saveTimeInPrefs(fullTime);
        // playing
        prefClass.setPlaying(true);

        // saveprefs
        log("saving in SetupTimer");
        //prefClass.saveAlarmTime(fullTime);

        NotificationManager notifyMgr =
                ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));

        // Delete dataItem and cancel a potential old countdown.
        cancelCountdown(notifyMgr);

        // Build notification and set it.
        notifyMgr.notify(Constants.NOTIFICATION_TIMER_COUNTDOWN, buildNotification(fullTime));

        // Register with the alarm manager to display a notification when the timer is done.
        registerWithAlarmManager(fullTime);

        if(mTimerThread != null){
            mTimerThread.cancelRunning();
            // remove prior thread
            handler.removeCallbacks(mTimerThread);

        }
        // create new thread and start
        mTimerThread = new ThreadTimer(handler, this, fullTime);
        mTimerThread.start();


    }

    private void stopAlarm() {
        log("stopped Alarm");
        Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    /**
     * Took this from Timer sample project
     * Build a notification including different actions and other various setup and return it.
     *
     * @param duration the duration of the timer.
     * @return the notification to display.
     */

    private Notification buildNotification(long duration) {
        // Intent to restart a timer.
        Intent restartIntent = new Intent(Constants.ACTION_RESTART_ALARM, null, this,
                TimerNotificationService.class);
        PendingIntent pendingIntentRestart = PendingIntent
                .getService(this, 0, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent to delete a timer.
        Intent deleteIntent = new Intent(Constants.ACTION_DELETE_ALARM, null, this,
                TimerNotificationService.class);
        PendingIntent pendingIntentDelete = PendingIntent
                .getService(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent startIntent = new Intent(Constants.ACTION_START, null, this,
                TimerNotificationService.class);
        startService(startIntent);

        // Create countdown notification using a chronometer style.
        return new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Time Left")
                .setContentText(TimerFormat.getTimeString(duration))
                .setUsesChronometer(true)
                .setWhen(System.currentTimeMillis() + duration)
                .addAction(android.R.drawable.ic_lock_idle_alarm, "Restart",
                        pendingIntentRestart)
                .addAction(android.R.drawable.ic_lock_idle_alarm, "Delete",
                        pendingIntentDelete)
                .setDeleteIntent(pendingIntentDelete)
                .setLocalOnly(true)
                .build();



        }
    /**
     * Took this from Timer sample project
     * Cancels an old countdown and deletes the dataItem.
     * @param notifyMgr the notification manager.
     */
    private void cancelCountdown(NotificationManager notifyMgr) {
        notifyMgr.cancel(Constants.NOTIFICATION_TIMER_EXPIRED);
    }

    private void registerWithAlarmManager(long duration) {

        // Calculate the time when it expires.
        long wakeupTime = System.currentTimeMillis() + duration;

        // Schedule an alarm.
        //alarm.setExact(AlarmManager.RTC_WAKEUP, 10000, pendingIntent);

        Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent,0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);


        // maybe send this to watchface?
        Intent newIntent = new Intent("com.miproducts.miwatch");
        newIntent.putExtra("MiWatch", wakeupTime);
        newIntent.setAction("com.miproducts.miwatch");
        newIntent.setPackage("com.miproducts.miwatch");
        sendBroadcast(newIntent);



    }

    public void setMinutes(int minutes){
        log("setMinutes with " + minutes);
        btvSecsToMin.setText(Integer.toString(minutes));
        //mTimerView.setMinutes(minutes);
    }

    public void setHours(int hours){
        btvMinToHr.setText(Integer.toString(hours));
       // mTimerView.setMinutes(hours);

    }
    private void log(String message) {
        Log.d(TAG, message);
    }

    // Toggles the Dismiss Overlay
    public void hideLayout(boolean hide){
        if(hide){
            btvMinToHr.setVisibility(View.GONE);
            btvSecsToMin.setVisibility(View.GONE);
            bReset.setVisibility(View.GONE);
            bStart.setVisibility(View.GONE);
            tvHr.setVisibility(View.GONE);
            tvMin.setVisibility(View.GONE);
            tvColon.setVisibility(View.GONE);
            flLayout.setVisibility(View.GONE);
            llDismissalOverlay.setVisibility(View.VISIBLE);
        }else {
            btvMinToHr.setVisibility(View.VISIBLE);
            btvSecsToMin.setVisibility(View.VISIBLE);
            bReset.setVisibility(View.VISIBLE);
            bStart.setVisibility(View.VISIBLE);
            tvHr.setVisibility(View.VISIBLE);
            tvMin.setVisibility(View.VISIBLE);
            tvColon.setVisibility(View.VISIBLE);
            flLayout.setVisibility(View.VISIBLE);
            llDismissalOverlay.setVisibility(View.GONE);
        }
    }

    // When the thread is counting time or when it is done we call these.
    protected void setHrTitle(String title){
        tvHr.setText(title);
    }
    protected void setMinTitle(String title){
        tvMin.setText(title);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentKillThread = new IntentFilter("Cancel_Timer");
        registerReceiver(brKillThread, intentKillThread);
    }
    // called by thread to reset everything
    public void resetHourAndMinute() {
        mTimerView.setMinutes(0);
        mTimerView.setHours(0);
    }

    // ThreadTimer calls this
    public void resetPrefAlarmTime() {
        prefClass.saveAlarmTime(0);
    }
    // called by the thread to save time when pausing.
    public void saveTimeInPrefs(long countDownInMinutes) {
        log("saving alarmTime in saveTimeInPrefs");
        prefClass.saveAlarmTime(countDownInMinutes);
    }

    public void informTimerViewOfDataChange() {
        log("informTimerViewOfDataChange");
       mTimerView.adjustTimeBasedOffOfPreferences();
    }
    public long getTimeInPrefs(){
        log("getTimeInPrefs");
        return prefClass.computeTimeLeftOnAlarm(System.currentTimeMillis());
    }

    public boolean wasPlaying() {
        return prefClass.wasPlaying();
    }
/*
    // called by thread to set the data times behind the TimerView
    public void sethoursSet(int hours){
        mTimerView.setHoursSet(hours);
    }
    // called by thread to set the data times behind the TimerView
    public void setMinutesSet(int minutes){
        mTimerView.setMinutesSet(minutes);
    }*/
}