package com.miproducts.mitimer.util;

/**
 * STORE Hr, Min, Sec - maybe even incorporate it in more than what im using.
 * Created by ladam_000 on 7/22/2015.
 */
public class TimeKeeper {
    private float hr = 0;
    private float min = 0;
    private float sec = 0;

    public TimeKeeper(float hr, float min, float sec){
        this.hr = hr;
        this.min = min;
        this.sec = sec;
    }
    public float getHr() {
        return hr;
    }

    public float getMin() {
        return min;
    }

    public float getSec() {
        return sec;
    }

    public void setHr(float hr) {
        this.hr = hr;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setSec(float sec) {
        this.sec = sec;
    }
}
