package com.miproducts.mitimer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by larry on 6/18/15.
 */
public class DismissOverlay extends View implements View.OnTouchListener{
    MainActivity mActivity;
    TimerView mTimerV;
    float xHalf, yHalf = 0;
    Paint mPaint, mPaintText,mPaintRect, mPaintBack;
    boolean isVisible = false;
    int sizeOfObjects = 100;
    int halfOfObjectSize = sizeOfObjects/2;
    Rect mRect, mRectBack;

    Bitmap bDismiss;
    Bitmap bDismissResized;

    public DismissOverlay(Context context) {
        super(context);
       // this.mTimerV = mTimerV;
        this.xHalf = xHalf;
        this.yHalf = yHalf;
        init(context);
    }

    public DismissOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {}

    public DismissOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DismissOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);

    }

    private void init(Context context) {
        log("init");
        mActivity = (MainActivity) context;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mActivity.getResources().getColor(android.R.color.holo_red_dark));

        mPaintText = new Paint();
        mPaintText.setColor(mActivity.getResources().getColor(android.R.color.white));
        mPaintText.setTextSize(sizeOfObjects);
        mPaintText.setAntiAlias(true);

        mPaintBack = new Paint();
        mPaintBack.setColor(mActivity.getResources().getColor(android.R.color.white));
        mPaintBack.setStyle(Paint.Style.FILL);
        mPaintBack.setAlpha(50);

        mPaintRect = new Paint();
        mPaintRect.setColor(mActivity.getResources().getColor(android.R.color.holo_green_dark));
        mPaintRect.setStyle(Paint.Style.FILL);

        //mPaintRect.setAlpha(50);
        setOnTouchListener(this);

    }

    public void setVisibility(boolean visible){
        log("set visibility to " + visible);
        isVisible = visible;
        mActivity.hideLayout(false);
        invalidate();

    }



    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            if(xHalf == 0){
                xHalf = canvas.getWidth()/2;
                yHalf = canvas.getHeight()/2;
                mRect = new Rect((int) xHalf-sizeOfObjects, (int) yHalf - sizeOfObjects, (int) xHalf + sizeOfObjects, (int) yHalf+sizeOfObjects);
                invalidate();
            } else {

                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaintBack);
                canvas.drawCircle(xHalf, yHalf, sizeOfObjects, mPaint);
                canvas.drawText("X", xHalf - 30, yHalf + 35, mPaintText);

        }

    }


    public boolean ExitOrCancelDismissOverlay(float x, float y){
        log("exitOrCancel");
        if(mRect.contains((int)x,(int)y))
            return true;
        else
            return false;
    }
    private void log(String msg){
     Log.d("DismissOverlay", msg);
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


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(ExitOrCancelDismissOverlay(event.getX(), event.getY())){
                    mActivity.finish();
                }else{
                    mActivity.hideLayout(false);
                }
                return true;
        }
        return false;
    }
}
