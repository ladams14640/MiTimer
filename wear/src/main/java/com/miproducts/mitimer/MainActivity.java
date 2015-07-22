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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.miproducts.mitimer.util.Constants;
import com.miproducts.mitimer.util.TimerFormat;

public class MainActivity extends Activity{
    private final String TAG = "MainActivity";
    public static final String ACTION = BuildConfig.APPLICATION_ID + ".action.ALARM";

    private TextView tvColon;
    private FrameLayout container;
    private TimerView mTimerView;

    DismissOverlay llDismissalOverlay;
    FrameLayout flLayout;

    // for exiting the app
    public DismissOverlayView mDismissOverlay;


    public Button bStart;
    public Button bReset;

    public BorderTextView btvMinToHr;
    public BorderTextView btvSecsToMin;

    TextView tvHr;
    TextView tvMin;

    int min = 60;
    int hr = 60;
    int convertToSecs = 1000;

    boolean isAlarmSet = false;

    // tell us to kill thread
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
                        // this awakens onInterceptDispatch.
                        // NOT GETTING ActionEvent.ON_MOVE without this
                        mTimerView.onTouchEvent(event);
                        // keep false to keep sending it down the chain and not absorbing.
                        flLayout.onTouchEvent(event);
                        return false;
                    }
                });


                initTextViews();



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


        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupTimer(mTimerView.getSetMinutes(), mTimerView.getSetHours());
                countDownTime = mTimerView.getSetMinutes()*(60*1000);
                beginCountDownThread();
                //finish();
            }
        });
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerView.setMinutes(0);
                mTimerView.setHours(0);
                mTimerView.resetArc();

                btvSecsToMin.setText("0");
                btvMinToHr.setText("0");

            }
        });
    }

    // temp keep track of the ccountDownTime
    // these three will handle the countdown display for the user.
    long countDownTime;
    final Handler handler = new Handler();
    Thread mTimerThread;

    private void beginCountDownThread() {
        countDownTime = mTimerView.getSetMinutes()*1000*60;
        // remove prior threads
        handler.removeCallbacks(mTimerThread);
        mTimerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    countDownTime += (-1000);
                    try {
                        Thread.sleep(1000);
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                // display minute version of it.
                                int countDownInMinutes = (int)countDownTime/1000/60;
                                // the alarm is less than 60 mins - lets have put min and seconds in
                                // hour and minutes.
                                if(countDownInMinutes < 60) {
                                    setHours(countDownInMinutes);
                                    // seconds
                                    setMinutes((int)countDownTime/1000%60);
                                    //TODO lets make sure we change the text under the hr and min to the appropriate stuff
                                    // todo then come back
                                }else {
                                    // TODO lets remove the seconds if we are over 60 minutes and show hr/min.
                                    setMinutes((int)(countDownTime/1000/60));

                                }
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        });
        mTimerThread.start();
    }
    //TODO stoped here setting up a thread to run when tyhe start button shows, the thread will continue to count down

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(mTimerThread);
        unregisterReceiver(brKillThread);
        super.onDestroy();

    }

    // Took this from Timer sample project
    // timer stuff all came from Android Timer project sample - why invent the wheel when they got it all there.
    private void setupTimer(long durSecs, long durMins) {


        log("duration =" + durSecs);
        long durationMillisSecs = durSecs * convertToSecs * min;
        long durationMillisMin = durMins * convertToSecs * min * hr;
        long fullTime = durationMillisSecs + durationMillisMin;


        NotificationManager notifyMgr =
                ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));

        // Delete dataItem and cancel a potential old countdown.
        cancelCountdown(notifyMgr);

        // Build notification and set it.
        notifyMgr.notify(Constants.NOTIFICATION_TIMER_COUNTDOWN, buildNotification(fullTime));

        // Register with the alarm manager to display a notification when the timer is done.
        registerWithAlarmManager(fullTime);
        isAlarmSet = true;

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
        btvSecsToMin.setText(Integer.toString(minutes));
    }

    public void setHours(int hours){
        btvMinToHr.setText(Integer.toString(hours));
    }
    private void log(String message) {
        Log.d(TAG, message);
    }


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


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentKillThread = new IntentFilter("Cancel_Timer");
        registerReceiver(brKillThread, intentKillThread);
    }

}