package com.example.lenovo.pingtuyouxi.mode;

import org.litepal.crud.DataSupport;



public class RankInfo extends DataSupport {
    private String time;
    private int pageCount;
    private float timeCount;
    private int pintusize;

    public int getPintusize() {
        return pintusize;
    }

    public void setPintusize(int pintusize) {
        this.pintusize = pintusize;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public float getTimeCount() {
        return timeCount;
    }

    public void setTimeCount(float timeCount) {
        this.timeCount = timeCount;
    }
}
