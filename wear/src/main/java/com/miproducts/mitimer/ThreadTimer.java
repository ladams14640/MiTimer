package com.miproducts.mitimer;

import android.os.Handler;

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
    // Keep track if we are running to kill thread.
    private boolean isRunning = true;
    // set the TextViews
    private boolean isTitleSet = false;



    ThreadTimer(Handler mHandler,MainActivity mActivity, long startingTime){
        this.mHandler = mHandler;
        this.mActivity = mActivity;
        this.timeCountDown = startingTime;
        //start();
    }

    @Override
    public void run() {
        while (isRunning) {
            timeCountDown += (-1000);
            try {
                Thread.sleep(1000);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // get the time
                        int countDownInMinutes = (int)timeCountDown/1000/60;
                        // the alarm is less than 60 mins - lets have put min and seconds in
                        // hour and minutes.
                        if(countDownInMinutes < 60) {
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

                        }
                        // Alarm is more than 60 mins
                        else {
                            //TODO #1.1 lets make sure we go deep Minute without moving, see whats going on there.
                            //TODO #1 lets make sure we reset arc or adjust it everytime we go back a second
                            //TODO #2 add a Pause and play - do we rebuild alarm for that? me thinks so
                            // TODO - #2 maybe wait till we remove the notification Function - hate that damn thing.
                            //mActivity.setHours();
                            // issue with ther math here. need Hours to come over too
                            mActivity.setHours((int) (timeCountDown / 1000 / 60 / 60));
                            mActivity.setMinutes((int) (timeCountDown / 1000 / 60 % 60));
                            // haven't set the titles yet
                            if(!isTitleSet){
                                isTitleSet = true;
                                mActivity.setHrTitle(HR);
                                mActivity.setMinTitle(MIN);
                            }


                        }

                        // Reset everything
                        if(timeCountDown <= 0){
                            killThread();

                        }
                        // we canceled Thread outside
                        if(!isRunning){
                            killThread();

                        }

                    }
                });
            } catch (Exception e) {
            }
        }

    }

    private void killThread() {
        mActivity.setMinutes(0);
        mActivity.setHours(0);
        mActivity.setHrTitle(HR);
        mActivity.setMinTitle(MIN);
        isTitleSet = false;
        //countDownInMinutes = 0;
        timeCountDown = 0;
        mActivity.resetHourAndMinute();
    }

    protected void cancelRunning() {
        isRunning = false;
    }
}
