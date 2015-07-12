package com.miproducts.mitimer.util;

import android.util.Log;

import com.miproducts.mitimer.MainActivity;
import com.miproducts.mitimer.TimerView;

/**
 * Created by ladam_000 on 6/16/2015.
 *
 * This class is used as a utility class, primarily used for the presses
 * I found that handling and generating values on factors like, isHour, isLongPress, what quadrant,
 * what values should that quadrant see, setting the minute and hour values, all is too meaty for the
 * timerView or the ArcTic.
 */
public class TimeValuesAndPressCalculator {
    private MainActivity mActivity;
    private TimerView mTimerView;

    public TimeValuesAndPressCalculator(MainActivity mActivity, TimerView timerView) {
        this.mActivity = mActivity;
        this.mTimerView = timerView;
    }
    public void log(String msg){
        Log.d("PressCalculator", msg);
    }


    public int handleShortPress(double theta, int quadrant, boolean isHour){
        // 00
        if(isFingerInZeroRange(theta)){
            log("00");
            // should draw a circle there or soemthing
                if(!isHour){
                    mTimerView.setMinutes(0);
                }else {
                    mTimerView.setHours(0);
                }
            return 1;
        }
        // 05
        else if(isFingerInFiveRange(theta)){
                if(!isHour){
                    mTimerView.setMinutes(5);
                }else {
                    mTimerView.setHours(1);
                }
            return 30;
        }
        // 10
        else if(isFingerInTensRange(theta)){
                if (!isHour) {
                    mTimerView.setMinutes(10);
                } else {
                    mTimerView.setHours(2);
                }
            return 60;
        }
        // 15
        else if(isFingerInFifteensRange(theta)){

                if (!isHour) {
                    mTimerView.setMinutes(15);
                } else {
                    mTimerView.setHours(3);
                }
            return 90;
        }
        // 20
        else if(isFingerInTwentyRange(theta)){

                if (!isHour) {
                    mTimerView.setMinutes(20);
                } else {
                    mTimerView.setHours(4);
                }

            return 120;
        }
        // 25
        else if(isFingerInTwentyFiveRange(theta)){

                if (!isHour) {
                    mTimerView.setMinutes(25);
                } else {
                    mTimerView.setHours(5);
                }

            return 150;
        }
        // 30
        else if(isFingerInThirtyRange(theta)){

                if (!isHour) {
                    mTimerView.setMinutes(30);
                } else {
                    mTimerView.setHours(6);

            }
            return 180;
        }
        // 35
        else if(isFingerInThirtyFiveRange(theta)){

                if (!isHour) {
                    mTimerView.setMinutes(35);
                } else {
                    mTimerView.setHours(7);

            }
            return 210;
        }
        // 40
        else if(isFingerInFortyRange(theta)){
                if (!isHour) {
                    mTimerView.setMinutes(40);
                } else {
                    mTimerView.setHours(8);
                }

            return 240;
        }
        // 45
        else if (isFingerInFortyFiveRange(theta)){
                if (!isHour) {
                    mTimerView.setMinutes(45);
                } else {
                    mTimerView.setHours(9);
                }

            return 270;
        }
        // 50
        else if(isFingerInFiftyRange(theta)){
                if(!isHour){
                    mTimerView.setMinutes(50);
                }else {
                    mTimerView.setHours(10);
                }
            return 300;
        }
        // 55
        else if(isFingerInFiftyFiveRange(theta)) {
                if (!isHour) {
                    mTimerView.setMinutes(55);
                } else {
                    mTimerView.setHours(11);
                }
                return 330;
            }
        return 0;
    }


    /**
     * This method is really meaty and it's where all of my logic comes from. I came up with all of
     * this. No part of the idea came from anything else I can think of. I am sure other programs
     * has this type of feature, but as the jargon I used to pick and describe what I am doing (DeepMinute)
     * I know no one else or program i can point to that I got this from.
     *
     * Diction:
     *
     * Park or Parking - I coined the term, when someone sits on a value for a certain amount of time
     * this will trigger DeepMinute mode, or isLongPress boolean and we will begin to display the
     * appropriate quadrant for the park.
     *
     * Quadrant - The clock is split up into 12 quadrants, each has 4 number sets, visually number
     * sets increment by 1 integer per hour mark on a clock.
     * The Quadrant the user is in depends on the number they began to initiate a DeepMinute or
     * isLongPress. So if the user parks on number 5 (on the 1hour mark on a clock), then he will
     * activate quadrant 5, which has numbers 5,6,7,8,9. Quadrants go up by intervals of 5s
     * (5,10,15,20). Quadrant 10 will have number sets of 10,11,12,13,14. The rule for Quadrants and
     * hour marks on a clock are as follows:
     *
     * first touch:
     * So from the joystick class we find out the theta (angle in relation to predetermined point on
     * screen) we pass in theta and quadrant, to this method handleShortPress - this will determine
     * what quadrant and minute we are at.
     *
     * then a longpress happens, cuz we are attempted to do DeepMinute, and so for instance we grabbed
     * a theta angle that I heuristically determined is quadrant 5, so first hour on a clock.
     * So we pass theta and the quadrant into handleLongPress and now we start to do even more magic.
     *
     * EX:
     * quadrant = 5;
     * if(quadrant == 5){
     *     if(theta is on 5)
     *          value = 5;
     *     else if(theta is on the 10)
     *          value = 6;
     *     else if(theta is on the 15)
     *         value = 7;
     *     else if(theta is on the 20)
     *         value = 8;
     *     else if(theta is on the 25)
     *         value = 9;
     * }
     *
     * so the golden rule is
     *
     * quadrant = #;
     * if(quadrant == #){
     *     if(theta is on #){
     *          value = #;
     *     else if(theta is on #+5)
     *          value = #+1;
     *     else if(theta is on #+10)
     *          value = #+2;
     *     else if(theta is on #+15)
     *          value = #+3;
     *     else if(theta is on #+20)
     *          value = #+4;
     *     }
     * }
     *
     * we check if theta is on the # or any of the clock hour numbers represented as 5 minute markers
     * and we determine the values and pass back the degrees the arc should be for that arc, we end up
     * using the appropriate arc depending on the mode we are in anyways, so we return it and set
     * values through the timerView instance.
     *
     * @param theta - the result of the Math.tan(x,y) from JoyStick class. It is in values like -3 to
     *              3 and then back from 3 to -3. As low as -3.99 and as high as 3.99. I heuristically
     *              determined through this method that if i get a theta value inbetween some constants
     *              it would mean that I should choose a number on a clock (like 5 or 10 or 15 or etc..)
     *              depending on the users touch, in relation to a predetermined point on the screen.
     * @param quadrant - quadrant we are in when the longpress was established. Will help us figure
     *                 out the true value and what bitmap to display when in a longPress and finding
     *                 out the desired time during DeepMinute choosing.
     * @return - the Angle in degrees that TicArc should be displaying.
     */

    public int handleLongPress(double theta, int quadrant){
        // this is far better - time to refactor like this.
        switch(quadrant){
            case 0:
                return handleQuadrantZero(theta);
            case 5:
                return handleQuadrantFive(theta);
           case 10:
               return handleQuadrantTen(theta);
            case 15:
                return handleQuadrantFifteen(theta);
            case 20:
                return handleQuadrantTwenty(theta);
            case 25:
                return handleQuadrantTwentyFive(theta);
            case 30:
                return handleQuadrantThirty(theta);
            case 35:
                return handleQuadrantThirtyFive(theta);
            case 40:
                return handleQuadrantForty(theta);
            case 45:
                return handleQuadrantFortyFive(theta);
            case 50:
                return handleQuadrantFifty(theta);
            case 55:
                return handleQuadrantFiftyFive(theta);
        }
        return 0;
        }

    // 0
    private boolean isFingerInZeroRange(double theta){
        return theta > Constants.zero_min && theta < Constants.zero_max;
    }
    // 5
    private boolean isFingerInFiveRange(double theta) {
        return (theta > Constants.five_min && theta <Constants.five_max);
    }
    // 10
    private boolean isFingerInTensRange(double theta) {
        return (theta > Constants.ten_min && theta <Constants.ten_max);
    }
    // 15
    private boolean isFingerInFifteensRange(double theta) {
        return (theta > Constants.fifteen_min && theta < Constants.fifteen_max);
    }
    // 20
    private boolean isFingerInTwentyRange(double theta) {
        return (theta > Constants.twenty_min && theta < Constants.twenty_max);
    }
    // 25
    private boolean isFingerInTwentyFiveRange(double theta) {
        return (theta > Constants.twentyFive_min && theta < Constants.twentyFive_max);
    }
    // 30
    private boolean isFingerInThirtyRange(double theta) {
        return (theta > Constants.thirty_min && theta < Constants.thirty_max);
    }
    // 35
    private boolean isFingerInThirtyFiveRange(double theta) {
        return (theta > Constants.thirtyFive_min && theta < Constants.thirtyFive_max);
    }
    // 40
    private boolean isFingerInFortyRange(double theta) {
        return (theta > Constants.forty_min && theta < Constants.forty_max);
    }
    //45
    private boolean isFingerInFortyFiveRange(double theta) {
        return (theta > Constants.fortyFive_min || theta < Constants.fortyFive_max);
    }
    //50
    private boolean isFingerInFiftyRange(double theta) {
        return (theta > Constants.fifty_min && theta < Constants.fifty_max);
    }

    //55
    private boolean isFingerInFiftyFiveRange(double theta) {
        return (theta > Constants.fiftyFive_min && theta < Constants.fiftyFive_max);
    }

    private int handleQuadrantZero(double theta){
        // number 1
        if(isFingerInZeroRange(theta)){
            log("1 - Deep");
            mTimerView.setMinutes(1);
            mTimerView.setSwingMinute(1); // mSwingAngleMin
            return 0;
        }
        // number 2
        else if(isFingerInFiveRange(theta)){
            log("2 - Deep");
            mTimerView.setMinutes(2);
            mTimerView.setSwingMinute(Constants.angleDifferential * 1); // mSwingAngleMin
            return 30;
        }
        // number 3
        else if(isFingerInTensRange(theta)){
            log("3 - Deep");
            mTimerView.setMinutes(3);
            mTimerView.setSwingMinute(Constants.angleDifferential * 2); // mSwingAngleMin
            return 60;
        }
        // number 4
        else if(isFingerInFifteensRange(theta)){
            log("4 - Deep");
            mTimerView.setMinutes(4);
            mTimerView.setSwingMinute(Constants.angleDifferential * 3); // mSwingAngleMin
            return 90;
        }
        // number 5
        else if(isFingerInTwentyRange(theta)){
            log("5 - Deep");
            mTimerView.setMinutes(5);
            mTimerView.setSwingMinute(30); // mSwingAngleMin
            return 120;
        }

        return 360;
    }


    private int handleQuadrantFive(double theta) {
        // number 5
        if(isFingerInFiveRange(theta)){
            log("5 - Deep");
            mTimerView.setMinutes(5);
            mTimerView.setSwingMinute(30); // mSwingAngleMin
            return 30;
        }
        // number 6
        else if(isFingerInTensRange(theta)){
            log("6 - Deep");
            mTimerView.setMinutes(6);
            mTimerView.setSwingMinute(30 + Constants.angleDifferential * 1); // mSwingAngleMin
            return 60;
        }
        // number 7
        else if(isFingerInFifteensRange(theta)){
            log("7 - Deep");
            mTimerView.setMinutes(7);
            mTimerView.setSwingMinute(30 + Constants.angleDifferential * 2); // mSwingAngleMin
            return 90;
        }
        // number 8
        else if(isFingerInTwentyRange(theta)){
            log("8 - Deep");
            mTimerView.setMinutes(8);
            mTimerView.setSwingMinute(30 + Constants.angleDifferential * 3); // mSwingAngleMin
            return 120;
        }
        // number 9
        else if(isFingerInTwentyFiveRange(theta)){
            log("9 - Deep");
            mTimerView.setMinutes(9);
            mTimerView.setSwingMinute(30 + Constants.angleDifferential * 4); // mSwingAngleMin
            return 150;
        }
        else return 0;
    }
    // return values 60,90,120,150,180
    private int handleQuadrantTen(double theta) {
        // number 10
        if(isFingerInTensRange(theta)){
            mTimerView.setMinutes(10);
            mTimerView.setSwingMinute(60); // mSwingAngleMin
            return 60;
        }
        // number 11
        else if(isFingerInFifteensRange(theta)){
            mTimerView.setMinutes(11);
            mTimerView.setSwingMinute(60 + Constants.angleDifferential * 1); // mSwingAngleMin
            return 90;
        }
        // number 12
        else if(isFingerInTwentyRange(theta)){
            mTimerView.setMinutes(12);
            mTimerView.setSwingMinute(60 + Constants.angleDifferential * 2); // mSwingAngleMin
            return 120;
        }
        // number 13
        else if(isFingerInTwentyFiveRange(theta)){
            mTimerView.setMinutes(13);
            mTimerView.setSwingMinute(60 + Constants.angleDifferential * 3); // mSwingAngleMin
            return 150;
        }
        // number 14
        else if(isFingerInThirtyRange(theta)){
            mTimerView.setMinutes(14);
            mTimerView.setSwingMinute(60 + Constants.angleDifferential * 4); // mSwingAngleMin
            return 180;
        }
        else return 0;
    }
    // return values 90,120,150,180,210
    private int handleQuadrantFifteen(double theta) {
        // number 15
        if(isFingerInFifteensRange(theta)){
            mTimerView.setMinutes(15);
            mTimerView.setSwingMinute(90); // mSwingAngleMin
            return 90;
        }
        // number 16
        else if(isFingerInTwentyRange(theta)){
            mTimerView.setMinutes(16);
            mTimerView.setSwingMinute(90 + Constants.angleDifferential * 1); // mSwingAngleMin
            return 120;
        }
        // number 17
        else if(isFingerInTwentyFiveRange(theta)){
            mTimerView.setMinutes(17);
            mTimerView.setSwingMinute(90 + Constants.angleDifferential * 2); // mSwingAngleMin
            return 150;
        }
        // number 18
        else if(isFingerInThirtyRange(theta)){
            mTimerView.setMinutes(18);
            mTimerView.setSwingMinute(90 + Constants.angleDifferential * 3); // mSwingAngleMin
            return 180;
        }
        // number 19
        else if(isFingerInThirtyFiveRange(theta)){
            mTimerView.setMinutes(19);
            mTimerView.setSwingMinute(90 + Constants.angleDifferential * 4); // mSwingAngleMin
            return 210;
        }
        else return 0;
    }

    // return values 120,150,180,210,240
    private int handleQuadrantTwenty(double theta) {
        int startNum = 20;
        int startSwing = 120;

        // number 20
        if(isFingerInTwentyRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 21
        else if(isFingerInTwentyFiveRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return startSwing+30;
        }
        // number 22
        else if(isFingerInThirtyRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return startSwing + 60;
        }
        // number 23
        else if(isFingerInThirtyFiveRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return startSwing + 90;
        }
        // number 24
        else if(isFingerInFortyRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return startSwing + 120;
        }
        else return 0;
    }

    // return values 150,180,210,240,270
    private int handleQuadrantTwentyFive(double theta) {
        int startNum = 25;
        int startSwing = 150;

        // number 25
        if(isFingerInTwentyFiveRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 26
        else if(isFingerInThirtyRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return startSwing+30;
        }
        // number 27
        else if(isFingerInThirtyFiveRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return startSwing + 60;
        }
        // number 28
        else if(isFingerInFortyRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return startSwing + 90;
        }
        // number 29
        else if(isFingerInFortyFiveRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return startSwing + 120;
        }
        else return 0;
    }

    // return values 180,210,240,270,300
    private int handleQuadrantThirty(double theta) {
        int startNum = 30;
        int startSwing = 180;

        // number 25
        if(isFingerInThirtyRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 26
        else if(isFingerInThirtyFiveRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return startSwing+30;
        }
        // number 27
        else if(isFingerInFortyRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return startSwing + 60;
        }
        // number 28
        else if(isFingerInFortyFiveRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return startSwing + 90;
        }
        // number 29
        else if(isFingerInFiftyRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return startSwing + 120;
        }
        else return 0;
    }

    // return values 210,240,270,300,330
    private int handleQuadrantThirtyFive(double theta) {
        int startNum = 35;
        int startSwing = 210;

        // number 25
        if(isFingerInThirtyFiveRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 26
        else if(isFingerInFortyRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return startSwing+30;
        }
        // number 27
        else if(isFingerInFortyFiveRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return startSwing + 60;
        }
        // number 28
        else if(isFingerInFiftyRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return startSwing + 90;
        }
        // number 29
        else if(isFingerInFiftyFiveRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return startSwing + 120;
        }
        else return 0;
    }

    // return values 240,270,300,330,350
    private int handleQuadrantForty(double theta) {
        int startNum = 40;
        int startSwing = 240;

        // number 25
        if(isFingerInFortyRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 26
        else if(isFingerInFortyFiveRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return startSwing+30;
        }
        // number 27
        else if(isFingerInFiftyRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return startSwing + 60;
        }
        // number 28
        else if(isFingerInFiftyFiveRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return startSwing + 90;
        }
        // number 29
        else if(isFingerInZeroRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return 360;
        }
        else return 0;
    }

    // return values 270,300,330,350,0
    private int handleQuadrantFortyFive(double theta) {
        int startNum = 45;
        int startSwing = 270;

        // number 25
        if(isFingerInFortyFiveRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 26
        else if(isFingerInFiftyRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return startSwing+30;
        }
        // number 27
        else if(isFingerInFiftyFiveRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return startSwing + 60;
        }
        // number 28
        else if(isFingerInZeroRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return 360;
        }
        // number 29
        else if(isFingerInFiveRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return 30;
        }
        else return 0;
    }
    // return values 300,330,350,360,30
    private int handleQuadrantFifty(double theta) {
        int startNum = 50;
        int startSwing = 300;

        // number 25
        if(isFingerInFiftyRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 26
        else if(isFingerInFiftyFiveRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return startSwing+30;
        }
        // number 27
        else if(isFingerInZeroRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return 0;
        }
        // number 28
        else if(isFingerInFiveRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return 30;
        }
        // number 29
        else if(isFingerInTensRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return 60;
        }
        else return 0;
    }

    // return values 330,350,360,5,10
    private int handleQuadrantFiftyFive(double theta) {
        int startNum = 55;
        int startSwing = 330;

        // number 25
        if(isFingerInFiftyFiveRange(theta)){
            mTimerView.setMinutes(startNum);
            mTimerView.setSwingMinute(startSwing); // mSwingAngleMin
            return startSwing;
        }
        // number 26
        else if(isFingerInZeroRange(theta)){
            mTimerView.setMinutes(startNum+1);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 1); // mSwingAngleMin
            return 0;
        }
        // number 27
        else if(isFingerInFiveRange(theta)){
            mTimerView.setMinutes(startNum+2);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 2); // mSwingAngleMin
            return 30;
        }
        // number 28
        else if(isFingerInTensRange(theta)){
            mTimerView.setMinutes(startNum+3);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 3); // mSwingAngleMin
            return 60;
        }
        // number 29
        else if(isFingerInFifteensRange(theta)){
            mTimerView.setMinutes(startNum+4);
            mTimerView.setSwingMinute(startSwing + Constants.angleDifferential * 4); // mSwingAngleMin
            return 90;
        }
        else return 0;

    }

}

