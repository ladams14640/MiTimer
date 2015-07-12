package com.miproducts.mitimer;

/**
 * Created by larry on 6/8/15.
 */
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class JoyStick {
    private final String TAG = "JoyStick";
    float ox, oy, radius;
    //Ship controllingObject;
    Paint nobBrush;
    Paint baseBrush;
    //SurfaceViewer ov;
    float jx, jy; // coordinates of the joystick's nob when moving
    float dx,dy; // coordinates of the new
    // the TicArc object
    private TimerView.TicArc mTicArc;

    public JoyStick(float _originX, float _originY,
                    int _radius, Paint base, Paint nob, TimerView.TicArc mArc) {
        //ov = theView;
        ox = _originX;
        oy = _originY;
        radius = _radius;
        baseBrush = base;
        nobBrush = nob;
        //controllingObject = myShip;
        jy = jx = 0;
        mTicArc = mArc;
    }

    public void onDraw(Canvas c) {
       // c.drawCircle(ox, oy, radius, baseBrush);
      //  if (jx == 0 & jy == 0)
           // c.drawCircle(ox, oy, radius / 3, nobBrush);
      //  else
           // c.drawCircle(jx, jy, radius / 3, nobBrush);


    }

    public void processTouch(float fx, float fy) {
        log("X is " + fx + "Y is " + fy);
        if (fx != 0 & fy != 0) {
            moveJoystick(fx, fy);
            //controllingObject.inputThrusters(true);
        } else {
            // controllingObject.inputThrusters(false);
            jx = 0;
            jy = 0;
        }

    }
    private void log(String message) {
        Log.d(TAG, message);
    }
    private void moveJoystick(float fx, float fy) {
         dx = (float) fx - ox;
         dy = (float) fy - oy;
        log("dx = " + dx + " dy = " + dy);
        float theta = (float) Math.atan2(dy, dx);
        log("Theta " + theta);
        float dist = (float) Math.sqrt((dx*dx)+(dy*dy));
        log("Dist " + dist);

        if(dist > radius){
            fx = (float) (ox + (radius * Math.cos(theta)));
            fy = (float) (oy + (radius * Math.sin(theta)));
            log("moving joystick cos = " + Math.cos(theta) + " sin = " +  Math.sin(theta));
            log("fx = " + fx + " fy = " + fy);
       }else{
            fx = ox+dx;
            fy = oy+dy;
        }
        jx = fx;
        jy = fy;
        log("jx, jy = " + jx + ", " + jy);
        mTicArc.adjustArcSize(theta);
    }
}