package com.miproducts.mitimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by ladam_000 on 6/14/2015.
 */
public class BorderTextView extends TextView {

    private boolean isSelected = true;
    private Rect mRect;
    private Context mContext;
    public float width = 66;
    public float height = 0;
    private Paint mPaint;
    private float cornerSize = 15;

    public BorderTextView(Context context) {
        super(context);
        init(context);
    }

    public BorderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BorderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public BorderTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
       }


    private void init(Context context){
        mContext = context;
        mPaint = new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.white));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(height == 0){
            width = 66;
            height = getHeight();
            Log.d("BorderTextView", "Width = " + width);
        }

        if(height != 0){
                canvas.drawRoundRect(0, 0, width, height, cornerSize, cornerSize, mPaint);
        }






    }

    @Override
    public boolean isSelected(){
        return isSelected;
    }

    public void setSelectedState(boolean selected){
        if(selected){
            mPaint.setColor(getResources().getColor(R.color.green));
            isSelected = true;
            invalidate();

        }
        else{
            mPaint.setColor(getResources().getColor(R.color.white));
            isSelected = false;
            invalidate();

        }
    }

}
