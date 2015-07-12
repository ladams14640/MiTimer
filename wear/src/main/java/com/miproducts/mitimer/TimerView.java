/**
 * Created by Laurence F. Adams III
 * Still working on, but finally refactoring and commenting everything, because I am reaching a
 * plateau here and just want to solidify everything while I am in the mood.
 *
 * Object, to set a timer for activities.
 * Found current app that comers with my watch and any in the market dont provide this easy, fluid
 * experience that we see with iwatch native timer app, so I am attempting to emulate their greatness.
 *
 * You select either Hour or Minute
 * Then drag your finger to the time you want, and hit start
 *
 * Added feature Deeper Minutes.
 * Deeper Minutes are minutes inbetween the Minute's circle.
 * The Minutes circle represents time by intervals of 5. (5,10,15), but users need minutes like 6 and
 * 7, so I found a way, if the user parks on a 5 or a 10 or w/e and waits long enough, then a deeper
 * Minutes circle will pop up giving them that number and the 4 succeeding that number, that way the
 * user gets the choice of any minute within an hr.
 */


package com.miproducts.mitimer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.miproducts.mitimer.util.Constants;
import com.miproducts.mitimer.util.TimeValuesAndPressCalculator;
import com.miproducts.mitimer.util.TimerFormat;

import java.sql.Time;

/**
 * Created by Larry on 6/4/2015.
 */
public class TimerView extends View implements OnTouchListener, View.OnLongClickListener {
    private final String TAG = "TimerView";
    private ImageView ivHour;
    /**
     *  grab a copy of the Activity, in order to manipulate the border of BorderTextViews displaying
     the HR and Minute
     */
    private MainActivity mActivity;

    private Context mContext;

    private Resources mResources;

    private Boolean isSeconds = true;

    private float xHalf, yHalf = 0;

    private Bitmap bImageMinute;
    private Bitmap bMinuteScaled;
    private Bitmap bImageHour;
    private Bitmap bHourScaled;

    private DeepMinute mDeepMinute;

    // object that displays the user's representation in time selection.
    private TicArc mTicArc;

    // TicArc's base location needs a rectangle, the circle fills in it.
    private RectF mRectF;

    /**
     * Joystick I made from ellaroids, figure out the theta (raw angle) of our
     * touches in order to represent it on the canvas for TicArc
    */
    private JoyStick testJoyStick;

    // seconds set for a timer - combined they determine how long the alarm is
    private int minuteSets = 0;
    private int hourSets = 0;

    /**
     * these are the rectangles where the Hours and Minutes selected BorderTextViews are
     * these are used in order to detect if a event.Down happens on one of the BorderTextViews
     * that way we know the user's intent is to alternate their time selection between minutes
     * and hours
     */
    Rect rectSecsToMin = null;
    Rect rectMinToHour;

    // whether the user has selected hours or is in the minutes selection circle
    private Boolean isHour = false;
    // if user is holding down his finger in a spot to provoke deeper minutes
    private boolean isLongPress = false;
    /**
     * minutes are displayed in intervals of 5s (0,5,10,15)
     * this value is set when a user sits on a digit long enough to provoke deeper minutes (1,2,3,..)
     * This value will be set in intervals of 5s (5,10,15), and so for example
     * 5 will allow us to narrow down on 5,6,7,8,9
     * 10 will allow us to narrow down on 10,11,12,13,14
     * 15 will allow us to narrow down on 15,16,17,18,19
     * and so on
     */
    private int currentZoomedInQuarter = 0;
    private Vibrator vibrator;

    float xDown;
    float xMove;
    float yDown;
    float yMove;
    float tDistance;

    TimeValuesAndPressCalculator pressCalculator;

    public TimerView(Context context) {
        super(context);
        init(null,0, context);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, context);

    }

    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {}


    private void init(AttributeSet attrs, int defStyle, Context context){
        mContext = context;
        mActivity = (MainActivity) mContext;
        initSeconds();
        setOnTouchListener(this);
        setOnLongClickListener(this);
        initJoyStick();
        pressCalculator = new TimeValuesAndPressCalculator(mActivity, this);
        vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
    }




    private void initSeconds() {
        bImageMinute = BitmapFactory.decodeResource(getResources(), R.drawable.t_minutes);
        bMinuteScaled = getResizedBitmap(bImageMinute, 250, 250);
        bImageHour = BitmapFactory.decodeResource(getResources(), R.drawable.t_hours);
        bHourScaled = getResizedBitmap(bImageHour, 250, 250);
    }
    private void initZoomedIn(int minutes){
        isLongPress = true;
        mDeepMinute.setQuadrant(minutes);
        currentZoomedInQuarter = minutes;
    }

    private void initJoyStick() {
        testJoyStick = new JoyStick(xHalf,yHalf,60,null, null, mTicArc);
    }

    private void initRect() {
        rectMinToHour = new Rect((int) xHalf -80, (int) yHalf -30, (int) xHalf-20, (int)yHalf+30);
        rectSecsToMin = new Rect((int) xHalf + 20, (int) yHalf -30, (int) xHalf+80, (int)yHalf+30);
        initSetSelection();

    }

    // grab a copy of the BorderTextView's we are using in MainActivity, because we change the border
    // color when one is selected
    private void initSetSelection() {
        mActivity.btvMinToHr.setSelectedState(false);
        mActivity.btvSecsToMin.setSelectedState(true);
        invalidate();
    }


    // Create the Arc that will display the representation of number current desired selection
    // this is whether we are adjusting Hours, Minutes, or Deeper Minutes.
    private void initArc(){
        log("initArcs");
        mRectF = new RectF(xHalf-125, yHalf-125, xHalf+125, yHalf+125);
        mTicArc = new TicArc(mContext, mRectF, 2);
        invalidate();
    }

    private void log(String message) {
        Log.d(TAG, message);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // lets set half of the canvas if we haven't yet.
        if(xHalf == 0){
            //log("xHalf == 0");
            xHalf = getWidth()/2;
            yHalf = getHeight()/2;
            initArc();
            initJoyStick();
            initRect();
            mDeepMinute = new DeepMinute(mContext, xHalf, this);
        }

            // Clock background - depending on  boolean values
            if (xHalf != 0) {
                // display Hour o'Clock
                if (isHour) {
                    canvas.drawBitmap(bHourScaled, xHalf - 125, 10, null);
                }
                // display Minute o'Clock
                else {
                    // display reg Minute
                    if (!isLongPress) canvas.drawBitmap(bMinuteScaled, xHalf - 125, 10, null);
                        // display deeper minutes
                    else {
                        mDeepMinute.setVisibility(true);
                        mDeepMinute.draw(canvas);

                    }
                }

            // draw the arc
            if (mTicArc != null) {
                mTicArc.draw(canvas);
            }
        }
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    //testJoyStick.processTouch(event.getX(), event.getY());
                    //invalidate();
                  //  log("TimerView onTouchEvent - Action Down");
                    break;
                case MotionEvent.ACTION_MOVE:
                  // testJoyStick.processTouch(event.getX(), event.getY());
                  //  invalidate();
                    //log("TimerView onTouchEvent - Action MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    //log("TimerView onTouchEvent - Action UP");
                //    testJoyStick.processTouch(0, 0);
                //    invalidate();
                    break;
                case MotionEvent.ACTION_CANCEL:
                //    testJoyStick.processTouch(0, 0);
                 //   invalidate();
                    //log("TimerView onTouchEvent - Action CANCEL");
                    break;
            }
            // this being true allows us to pass the touches all to the onTouch - which has the view
            return true;
        }

    final Handler handLongPress = new Handler();

    Runnable mLongPressed = new Runnable() {
        public void run() {
            log("Long press!");
            vibrate(false);
            initZoomedIn(minuteSets);
            // SHOULD LIGHT VIBRATE
        }
    };

    final Handler handVeryLongPress = new Handler();

    Runnable mVeryLongPressed = new Runnable() {
        public void run() {
            vibrate(true);

            log("VeryLong press!");
            mActivity.hideLayout(true);
            invalidate();
            // SHOULD LIGHT VIBRATE
        }
    };
    private void vibrate(boolean veryLong) {
        if(!veryLong) {
            // 1 - don't repeat
            final int indexInPatternToRepeat = 1;
            //vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
            vibrator.vibrate(100);
        }else {
            // 1 - don't repeat
            final int indexInPatternToRepeat = 1;
            //vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
            vibrator.vibrate(250);
        }

    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //log("TimerViews onTOuchListener");
        //log("onTouch id of view is " + v.getId());

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xDown = event.getX();
                yDown = event.getY();

                // cancel the Runnables incase.
                handLongPress.removeCallbacks(mLongPressed);
                handVeryLongPress.removeCallbacks(mVeryLongPressed);
                // lets set a Runnable if we reach the 1 second
                handLongPress.postDelayed(mLongPressed, Constants.THRESHOLD_LONGPRESS);
                handVeryLongPress.postDelayed(mVeryLongPressed, Constants.THRESHOLD_VERY_LONGPRESS);



                // isTouch in the minute spot on the screen
                if (rectSecsToMin.contains((int) event.getX(), (int) event.getY())) {
                    mActivity.btvSecsToMin.setSelectedState(true);
                    mActivity.btvMinToHr.setSelectedState(false);
                    isHour = false;
                    log("secsToMin Touched");
                    invalidate();
                    return true;

                }
                // isTouch in the hr spot on the screen
                else if (rectMinToHour.contains((int) event.getRawX(), (int) event.getRawY())) {
                    mActivity.btvMinToHr.setSelectedState(true);
                    mActivity.btvSecsToMin.setSelectedState(false);
                    isHour = true;
                    log("minToHour Touched");
                    invalidate();

                    return true;
                }
                else {
                      // lets move the arc
                      testJoyStick.processTouch(event.getX(), event.getY());
                  }
                    invalidate();

                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getX();
                yMove = event.getY();

                // remove the longPressHandler
                float dist = Math.abs((xMove - xDown) + (yMove - yDown));
                if(dist > 25){
                    handLongPress.removeCallbacks(mLongPressed);
                    handVeryLongPress.removeCallbacks(mVeryLongPressed);
                }

                testJoyStick.processTouch(event.getX(), event.getY());
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                fingerOff();
                return true;
            case MotionEvent.ACTION_CANCEL:
                fingerOff();
                return true;        }

        return true;
    }

    private void fingerOff() {
        testJoyStick.processTouch(0, 0);
        xDown = 0;
        yDown = 0;
        yMove =0;
        yDown = 0;
        tDistance = 0;
        // no longer displaying the Deep Minute Quadrants
        mDeepMinute.setVisibility(false);
        handVeryLongPress.removeCallbacks(mVeryLongPressed);
        handLongPress.removeCallbacks(mLongPressed);

        if(isLongPress){
            isLongPress = false;
            mTicArc.invalidate();
        }
        invalidate();
    }



    @Override
    public boolean onLongClick(View v) {
        log("long click");
        return true;
    }


    /**
     * TicArc
     */
    public class TicArc extends View  {
        private RectF mRectF;
        private float mSwingAngleMin;
        private float mSwingAngleHr;
        private Paint mPaint;
        public Boolean isVisible = false;
        private float mSwingAngleDeepMin;


        public TicArc(Context context, RectF rectF, int startAngle) {
            super(context);
            //log("TicArc with " + startAngle + " being the startAngle");
            init(null, 0, context, rectF, startAngle);
        }

        public TicArc(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(attrs, 0, context, null, 0);
        }

        public TicArc(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(attrs, defStyleAttr, context, null, 0);
        }


        private void init (AttributeSet attrSet, int defStyleAttr, Context context, RectF rectF, float swingAngle){
            mContext = context;
            mRectF = rectF;
            mSwingAngleMin = swingAngle;

            mPaint = new Paint();
            mPaint.setColor(getResources().getColor(R.color.orange));
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(5);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            log("Tic onDraw");
               // log("drawing Tic Arc with start Angle of " + mStartAngle);
                // we will pass in the startAngle, always increase by 1 degree.
            if(!isHour) {
                if (!isLongPress)
                canvas.drawArc(mRectF, 270, mSwingAngleMin, false, mPaint);
                else
                canvas.drawArc(mRectF, 270, mSwingAngleDeepMin, false, mPaint);
            }
            else {
                canvas.drawArc(mRectF, 270, mSwingAngleHr, false, mPaint);

            }
        }


        // this method is called in the JoyStick's movement, to set the arc's value and invalidate.
        public void adjustArcSize(float theta) {
            log("adjust arc size");
            if(!isHour){
                if(!isLongPress)
                    mSwingAngleMin = convertThetaToArcDegrees(theta);
                else mSwingAngleDeepMin = convertThetaToArcDegrees(theta);
            }else {
                    mSwingAngleHr = convertThetaToArcDegrees(theta);
            }
            invalidate();
        }


        /**
         * 1. this seems to be where all my logic is going into, for figuring out values for
         *  1.1 minutes or hours - depending on if isHour or not
         *  1.2 minutes (interval of 5s) or deep minutes (intervals of 1s) - if we are setting deep
         *      minutes then we will also save the representation of the minutes for when we come back
         *      out of the deep minutes feature, so we will have it rdy for display
         *
         * @param theta - is the angle we get from the joystick.
         * @return an Angle in degrees, used to display the user's representation of time on a circle
         */
       private float convertThetaToArcDegrees(float theta) {
           /*
           * so at this point
           * 1. initZoomed was called when onMove hit parameters to consider parked - isLong is now set to true
           * 2. quadrant picked out
           * 3. image has been made
           * 4. image can be displayed whenever
           * */

           if(isLongPress){
               // handle, by sending it all to Deep Minute
               // otherwise proceed.
               mDeepMinute.setVisibility(true);
              return pressCalculator.handleLongPress(theta, currentZoomedInQuarter);
           }
           // not a long click
           else
           {
               mDeepMinute.setVisibility(false);
               return pressCalculator.handleShortPress(theta, currentZoomedInQuarter, isHour);
           }

        }

        public void resetArc(){
            log("Tic Arc resetArc");
            mSwingAngleMin = 1;
            mSwingAngleDeepMin = 1;
            mSwingAngleHr = 1;
        }
    }

    public void setMinutes(int minutes){
        minuteSets = minutes;
        mActivity.setMinutes(minutes);
    }

    public void setHours(int hours){
        hourSets = hours;
        mActivity.setHours(hours);
    }

    public int getSetMinutes(){
        return minuteSets;
    }

    public int getSetHours(){
        return hourSets;
    }

    public void setSwingMinute(int swingAngle){
        mTicArc.mSwingAngleMin = swingAngle;
    }
    public void resetArc(){
        log("reset Arc");
        mTicArc.resetArc();
        invalidate();
    }
}
