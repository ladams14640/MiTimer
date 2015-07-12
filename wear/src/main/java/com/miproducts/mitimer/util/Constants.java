package com.miproducts.mitimer.util;

/**
 * Created by larry on 6/12/15.
 */
import android.net.Uri;

/** Used to hold constants. */
public final class Constants {

    public static final String START_TIME = "timer_start_time";
    public static final String ORIGINAL_TIME = "timer_original_time";
    public static final String DATA_ITEM_PATH = "/timer";
    public static final Uri URI_PATTERN_DATA_ITEMS =
            Uri.fromParts("wear", DATA_ITEM_PATH, null);

    public static final int NOTIFICATION_TIMER_COUNTDOWN = 1;
    public static final int NOTIFICATION_TIMER_EXPIRED = 2;

    public static final String ACTION_SHOW_ALARM
            = "com.android.example.clockwork.timer.ACTION_SHOW";
    public static final String ACTION_DELETE_ALARM
            = "com.android.example.clockwork.timer.ACTION_DELETE";
    public static final String ACTION_RESTART_ALARM
            = "com.android.example.clockwork.timer.ACTION_RESTART";
    public static final String ACTION_START = "START";

    public static final int THRESHOLD_LONGPRESS = 250;

    public static final int THRESHOLD_VERY_LONGPRESS = 1250;

    public static final int THRESHOLD_LONGPRESS_CANCEL_DRAG = 75;


    public static final int angleDifferential = 6;

    /**
     * All of the degrees that spit out of tan equation of the joystick
     */
    public static final double zero_min = -1.75;
    public static final double zero_max = -1.25;
    public static final double five_min = -1.25;
    public static final double five_max = -.75;
    public static final double ten_min = -.75;
    public static final double ten_max = -.25;

    public static final double fifteen_min = -.25;
    public static final double fifteen_max = .25;
    public static final double twenty_min = .25;
    public static final double twenty_max = .75;

    public static final double twentyFive_min = .75;
    public static final double twentyFive_max = 1.25;
    public static final double thirty_min = 1.25;
    public static final double thirty_max = 1.75;

    public static final double thirtyFive_min = 1.75;
    public static final double thirtyFive_max = 2.25 ;
    public static final double forty_min = 2.25;
    public static final double forty_max = 2.75;

    public static final double fortyFive_min = 2.75;
    public static final double fortyFive_max = -2.75;
    public static final double fifty_min = -2.75;
    public static final double fifty_max = -2.25;

    public static final double fiftyFive_min = -2.25;
    public static final double fiftyFive_max = -1.75;










    private Constants() {
    }

}