package com.miproducts.mitimer;

import android.os.Handler;
import android.util.Log;
//TODO #END must refactor
/**
 * Created by larry on 7/22/15.
 */
public class ThreadTimer extends Thread
{
    private static final String MIN = "MIN";
    private static final String HR = "HR";
    private static final String SEC="SEC";

    private Handler mHandler;
    private MainActivity mActivity;
    // count down
    private long timeCountDown;
    // minute count down
    private int countDownInMinutes;
    // Keep track if we are running to kill thread.
    private boolean isRunning = true;
    // set the TextViews
    private boolean isTitleSet = false;
    // user initiated a pause
    private boolean pausing = false;


    ThreadTimer(Handler mHandler,MainActivity mActivity, long startingTime){
        this.mHandler = mHandler;
        this.mActivity = mActivity;
        this.timeCountDown = startingTime;
        //start();
    }

    @Override
    public void run() {
        while (isRunning) {
           // log("while");
            try {
                Thread.sleep(1000);
            }catch (Exception e) {
            }

                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        timeCountDown += (-1000);
                       // log("running");
                        // get the time
                        countDownInMinutes = (int)timeCountDown/1000/60;
                        // user declared he wanted to pause the app in mActivity.bStart.setOnClick
                        if(pausing){
                            log("pausing");
                            killThread();
                        }else {
                            // the alarm is less than 60 mins - lets have put min and seconds in
                            // hour and minutes.
                            if(countDownInMinutes < 60) {
                                //log("countDowninMinutes less than 60");
                                // haven't set the titles yet
                                if(!isTitleSet){
                                    isTitleSet = true;
                                    mActivity.setHrTitle(MIN);
                                    mActivity.setMinTitle(SEC);
                                }
                                // minutes
                                mActivity.setHours(countDownInMinutes);
                                // seconds
                                mActivity.setMinutes((int) timeCountDown / 1000 % 60);
                            }// Alarm is more than 60 mins
                            else if(countDownInMinutes > 60){
                                //   log("minutes above 60");
                                //TODO #1.1 lets make sure we go deep Minute without moving, see whats going on there.
                                //TODO #1 lets make sure we reset arc or adjust it everytime we go back a second
                                //TODO #2 add a Pause and play - do we rebuild alarm for that? me thinks so
                                // TODO - #2 maybe wait till we remove the notification Function - hate that damn thing.
                                mActivity.setHours((int) (timeCountDown / 1000 / 60 / 60));
                                mActivity.setMinutes((int) (timeCountDown / 1000 / 60 % 60));
                                // haven't set the titles yet
                                if(!isTitleSet){
                                    isTitleSet = true;
                                    mActivity.setHrTitle(HR);
                                    mActivity.setMinTitle(MIN);
                                }
                            }
                            //TODO acting wierd when i switch this to a if - figure out why my thread wont
                            // show seconds if so and fix it.
                            // Reset everything
                            else if(timeCountDown <= 0){
                                log("timeCountdown == 0");
                                killThread();
                            }
                            // we canceled Thread outside
                            else if(!isRunning){
                                log("no longer running");
                                killThread();

                            }


                        }


                    }
                });
            }
        }



    private void killThread() {
        log("killThread");

        // regardless we are not running
        isRunning = false;

        // need the time difference between now and the time we are counting down to!
        long timeToSave = timeCountDown;
        log("killThread - saving Prefs of " + timeToSave);
        log("killThread - saving prefs System time of = " + System.currentTimeMillis());
        mActivity.saveTimeInPrefs(timeToSave);
        mActivity.saveLastSystemTime(System.currentTimeMillis());

        // tell timerView to set their values based off of preference, since we just saved the time there.
        mActivity.informTimerViewOfDataChange();



        // dont reset any of this if we are just pausing.
        // want to show all the stuff still.
        if(pausing){
            log("just Pausing.");
            pausing = false;

        }
        // clear everything.
        else {
            log("clear everything, we aint pausing we killing");
            mActivity.setMinutes(0);
            mActivity.setHours(0);
            mActivity.setHrTitle(HR);
            mActivity.setMinTitle(MIN);
            isTitleSet = false;
            //countDownInMinutes = 0;
            timeCountDown = 0;
            mActivity.resetHourAndMinute();
            mActivity.resetPrefAlarmTime();

        }
        interrupt();
    }

    private void log(String s) {
        Log.d("ThreadTimer", s);
    }

    protected void cancelRunning() {
        isRunning = false;
    }

    protected boolean isRunning(){
        return isRunning;
    }

    // lets tell thread to save stuff
    public void pauseRunning() {
        pausing = true;
    }
    /* get the current time. */
    public long getCountDownTime() {
        return timeCountDown;
    }
}
