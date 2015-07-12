package com.miproducts.mitimer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by ladam_000 on 6/16/2015.
 *
 * Will display the appropriate quadrant (pie is 12 pieces tho), each quadrant containing 5 Numbers
 * representing Deeper Minutes than the 5 minute interval normal display we have.
 * /**
 * minutes are normally displayed in intervals of 5s (0,5,10,15)
 * this value is set when a user sits on a digit long enough to provoke deeper minutes (1,2,3,..)
 * This value will be set in intervals of 5s (5,10,15), and so for example
 * 5 will allow us to narrow down on 5,6,7,8,9
 * 10 will allow us to narrow down on 10,11,12,13,14
 * 15 will allow us to narrow down on 15,16,17,18,19
 * and so on
 *
 *
 * We need
 * 1. quadrant we will be representing
 * 2. the image we are using
 * 3.
 */

public class DeepMinute  extends View{
    // should be 5,10,15, etc...
    private int quadrant;
    private Context mContext;
    private boolean isVisible = false;
    private Bitmap bZoomedIn;
    private Bitmap bZoomedInScaled;
    private float xHalfOfScreen;
    private MainActivity mActivity;
    private TimerView mTimerView;

    public DeepMinute(Context context, float xHalf, TimerView mTimerView) {
        super(context);
        init(context, xHalf, mTimerView);
    }

    public DeepMinute(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public DeepMinute(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    public DeepMinute(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);

    }
    // image to grab
    // isDisplaying
    //


    private void init(Context context, float xHalf, TimerView mTimerView){
        mContext = context;
        xHalfOfScreen = xHalf;
        mActivity = (MainActivity) context;
        this.mTimerView = mTimerView;

    }
    private void init(Context context){
        log("used context without passing in xHalf");
        mContext = context;
        mActivity = (MainActivity) context;
        // just a guess
        xHalfOfScreen = 150;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(bZoomedInScaled != null)
            if(isVisible)
                canvas.drawBitmap(bZoomedInScaled, xHalfOfScreen-125, 10, null);
    }

    public void setVisibility(boolean visible){
        isVisible = visible;
        invalidate();
    }

    public boolean isVisible(){
        return isVisible;
    }

    public void setQuadrant(int quad){
        quadrant = quad;
        switch(quad) {
            case 0:
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_one_to_five);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 5:
                //currentZoomedInQuarter = 5;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_five_to_ten);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 10:
                //currentZoomedInQuarter = 10;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_ten_to_fourteen);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 15:
               // currentZoomedInQuarter = 15;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_fifteen_to_nineteen);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 20:
          //      currentZoomedInQuarter = 20;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_twenty_to_twentyfour);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 25:
       //         currentZoomedInQuarter = 25;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_twentyfive_to_twentynine);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 30:
         //       currentZoomedInQuarter = 30;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_thirty_to_thirtyfour);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 35:
        //        currentZoomedInQuarter = 35;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_thirtyfive_to_thirtynine);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 40:
        //        currentZoomedInQuarter = 40;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_forty_to_fortyfour);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 45:
         //       currentZoomedInQuarter = 45;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_fortyfive_to_fortynine);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 50:
       //         currentZoomedInQuarter = 50;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_fifty_to_fiftyfour);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
            case 55:
      //          currentZoomedInQuarter = 55;
                bZoomedIn = BitmapFactory.decodeResource(getResources(), R.drawable.t_min_fiftyfive_to_fiftynine);
                bZoomedInScaled = getResizedBitmap(bZoomedIn, 250, 250);
                break;
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
    private void log(String msg){
        Log.d("DeepMinute", msg);
    }
}
