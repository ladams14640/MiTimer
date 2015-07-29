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
import android.widget.Toast;

import com.miproducts.mitimer.util.Constants;
import com.miproducts.mitimer.util.TimeKeeper;
import com.miproducts.mitimer.util.TimerFormat;

public class MainActivity extends Activity{
    private final String TAG = "MainActivity";
    public static final String ACTION = BuildConfig.APPLICATION_ID + ".action.ALARM";
    // intent filter and intent to kill thread from notification.
    public static final String KEY_INTENT_KILL_THREAD = "Cancel_Timer";

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
    /* Boolean set by user when he begins to set the time with the TicArc, if he has adjusted time
    we wont start the timer with the saved time, but rather with a new time. This is determined in the
    Start button's onClick Handler.  */
    private boolean timeAdjusted = false;


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
         *
         * We click on start in with 3 possible intentions:
         * 1. we just set the time we want for an alarm and now start it.
         * 2. we already started the time and we want to pause it.
         * 3. it was paused and we want to restart it.
         *
         * 1. If alarm is not already set and nothing is saved, we assume to grab time from TimerView's set data, tell Prefs we have set alarm and the boolean as well.
         * 2. Alarm was already set and we need to pause where we are. cancel countdwn, save isAlarmSet in Prefs, set boolean isAlarmSet to false, kill thread and notification.
         * 3. check to make sure nothing was saved, and tiem was not adjusted by the TimeViewer's TicArc.  if so initialize setup process.
         */
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Alarm is not set so start time
                if(!isAlarmSet){
                   // log("Start");
                    // either way these need to get done
                    bStart.setText("Pause");
                    //TODO might not need isAlarmSet, just use the preference boolean, since it is relied on by other classes.
                    isAlarmSet = true;
                    prefClass.setPlaying(true);
                    // get the alarmTime - still need to add it to the system time to get the countdown time
                    long alarmTime = prefClass.getAlarmTime();
                    //log("got alarmTime from preference " + alarmTime);

                    // preference had something saved. and time was not recently adjusted by the user.
                    if(alarmTime != 0 && !timeAdjusted){
                        // get HR, MIN, SEC for display.
                        timeKeeper = TimerFormat.breakDownMilliSeconds(alarmTime);
                        //TODO #ISSUE when we unPause mins and secs become hrs and mins.
                        // there was hrs stored.
                        if(timeKeeper.getHr() != 0) {
                            //log("hr and minute of the timeKeeper is " + (int) timeKeeper.getHr() + " " + (int) timeKeeper.getMin());
                            setupTimer(true, (int) timeKeeper.getMin(), (int) timeKeeper.getHr());
                            //prefClass.saveAlarmTime(System.currentTimeMillis() + countDownTime);
                        }
                        // there was minutes and seconds stored.
                        else{
                            //log("Minute and Seconds of the timeKeeper is " + (int) timeKeeper.getMin() + " " + (int) timeKeeper.getSec());

                            setupTimer(false, (int) timeKeeper.getSec(), (int) timeKeeper.getMin());
                        }
                    }

                    // preference had nothing saved or time was adjusted.
                    else {
                        // time has not been adjusted anymore, now that time has been set.
                        timeAdjusted = false;
                        // make sure the user didnt just have a 0 in these BTVs.
                        if(mTimerView.getSetMinutes() != 0 || mTimerView.getSetHours() != 0){
                            // setup Alarm
                            setupTimer(true, mTimerView.getSetMinutes(), mTimerView.getSetHours());
                        }else {
                            Toast.makeText(getApplication(), "Please set a Time", Toast.LENGTH_SHORT).show();
                            // turn these
                            bStart.setText("Start");
                            prefClass.setPlaying(false);
                            isAlarmSet = false;
                        }
                    }



                }
                // Alarm was already set, assume that we just clicked to Pause
                else {
                    //log("Pause");
                    cancelCountdownNotification();
                    prefClass.setPlaying(false);

                    isAlarmSet = false;
                    timeAdjusted = false;
                    //TODO tesrting this - hr is acting wierd for saving and even Pausing.
                    if(mTimerThread != null)
                        mTimerThread.pauseRunning();

                    // thread will save to prefs the remaining time.
                    killThread();
                    mTimerThread = null;

                    bStart.setText("Start");
                    stopAlarm();
                }

            }
        });
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCountdownNotification();
                prefClass.setPlaying(false);
                isAlarmSet = false;
                timeAdjusted = false;
                // handle thread
                killThread();
                mTimerThread = null;



                // no longer are we paused, if we were.
                bStart.setText("Start");
                mTimerView.resetArc();
                // reset BorderTextView display's
                setMinutes(0);
                setHours(0);
                // reset title of the two BorderTextViews
                setHrTitle("HR");
                setMinTitle("MIN");

                saveTimeInPrefs(0);
                saveLastSystemTime(0);

                resetHourAndMinute();
                resetPrefAlarmTime();
                mTimerView.adjustTimeBasedOffOfPreferences();

            }
        });
    }

    private void cancelCountdownNotification() {
        //log("CancelCountdownNotificatioj");

        NotificationManager notifyMgr =
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notifyMgr.cancel(Constants.NOTIFICATION_TIMER_COUNTDOWN);

    }

    //TODO refactor this is seriously meaty - maybe too much?
    // grab old alarmtime and compare to now
    private void grabCurrentAlarmTime() {
        //log("grabCurrentAlarm");

        // get the alarmTime - still need to add it to the system time to get the countdown time
        long alarmTime = prefClass.getAlarmTime();
        //log("got alarmTime from preference " + alarmTime);

        // Something was saved lets startup.
        if(alarmTime != 0){
          boolean wasPlaying = prefClass.wasPlaying();
            // That which was newNum = (alarmTime + lastCurrentTime) - currentTime should > 0
            // if its > 0 then lets use that new num else newNum = 0; because time elapsed too far.

            // Grab last saved time Stamp
            long lastSavedTime = prefClass.getLastSystemTime();

            long currentSystemTime = System.currentTimeMillis();
            long diffInTime = currentSystemTime - lastSavedTime;
            long diffInAlarm = alarmTime - diffInTime;

            // Save the time now, because if setupTimer is called - due to time being paused - then
            // it will grab old data, we want the fresh alarm now!
            saveTimeInPrefs(diffInAlarm);

            log("last Saved Time = " + lastSavedTime);
            log("currentTime = " + currentSystemTime);
            log("diffInTime = " + diffInTime);
            log("diffInAlarm = " + diffInAlarm);


            // the time has not exceeded the alarm duration
            if(diffInAlarm > 0){
                    log("got time left");
                    // was playing so we want to continue playing
                    // get HR, MIN, SEC for display.
                    timeKeeper = TimerFormat.breakDownMilliSeconds(diffInAlarm);
                    // we were playing.
                    if(wasPlaying) {
                        isAlarmSet = true;
                        bStart.setText("Pause");




                        // there was hrs stored.
                        if (timeKeeper.getHr() != 0) {
                            //log("hr and minute of the timeKeeper is " + (int) timeKeeper.getHr() + " " + (int) timeKeeper.getMin());
                            //TODO do math here to subtract current time from the total time.


                            setupTimer(true, (int) timeKeeper.getMin(), (int) timeKeeper.getHr());
                            //prefClass.saveAlarmTime(System.currentTimeMillis() + countDownTime);
                        }
                        // there was minutes and seconds stored.
                        else {
                            //log("Minute and Seconds of the timeKeeper is " + (int) timeKeeper.getMin() + " " + (int) timeKeeper.getSec());
                            //TODO do math here to subtract current time from the total time.
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
            // the time has exceeded the alarm duration
            else {
                log("no time left.");
                isAlarmSet = false;
                bStart.setText("Start");
            }

        }
        // nothing saved.
        else {
            isAlarmSet = false;
            bStart.setText("Start");

        }
    }
    // annoying that i get wierd results when screen dims by user's palm so ima try this.
    @Override
    protected void onPause() {
        unregisterReceiver(brKillThread);
        // must check - otherwise will crash if we dont have a thread, because we paused it.
        if(mTimerThread != null)
            saveTimeInPrefs(mTimerThread.getCountDownTime());
        prefClass.saveLastSystemTime(System.currentTimeMillis());
        killThread();
        super.onPause();
    }
    // moved grab current alarm time here because it kicks off thread for us if we need,
    // even when just paused and coming back.


    @Override
    protected void onRestart() {
        grabCurrentAlarmTime();
        super.onRestart();
    }
/*
    @Override
    protected void onDestroy() {
        unregisterReceiver(brKillThread);
        // must check - otherwise will crash if we dont have a thread, because we paused it.
        if(mTimerThread != null)
            saveTimeInPrefs(mTimerThread.getCountDownTime());
        prefClass.saveLastSystemTime(System.currentTimeMillis());
        killThread();
        super.onDestroy();
    }
*/
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

        killThread();
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
        log("setHours with " + hours);
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
        IntentFilter intentKillThread = new IntentFilter(KEY_INTENT_KILL_THREAD);
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
    /* Called in TimerView by TicArc when joystick is moving this is called at the same time to tell
     * MainActivity that time has changed and not to start a timer with saved time, but rather with the
     * new time at hand.*/
    public void timeAdjustedByUser() {
        timeAdjusted = true;
    }
    /* called by timerView to kill thread and to reset values
       reset TimerView's hold on the data it uses to display through the BTVs.
       reset the alarm that is saved in preferences.
     */
    public void killThread() {
        if (mTimerThread != null) {
            mTimerThread.cancelRunning();
            // keeps resetng the time
            mTimerThread.interrupt();
        }
        handler.removeCallbacks(mTimerThread);
    }
    /* Called by ThreadTimer to save time */
    public void saveLastSystemTime(long l) {
        prefClass.saveLastSystemTime(System.currentTimeMillis());

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