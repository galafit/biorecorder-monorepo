package com.biorecorder.bichart.configs;


import com.biorecorder.bichart.graphics.BColor;

/**
 * Created by galafit on 1/10/17.
 */
public class ScrollConfig {
    private BColor color = BColor.GRAY;
    private int touchRadius = 10; //px

    public ScrollConfig() {
    }

    public ScrollConfig(ScrollConfig config) {
        touchRadius = config.touchRadius;
        color = config.color;

    }

    public BColor getColor() {
         return new BColor(color.getRed(), color.getBlue(), color.getGreen(), 250);
     }

    public void setColor(BColor color) {
        this.color = color;
    }

    public void setTouchRadius(int activeExtraSpace) {
        this.touchRadius = activeExtraSpace;
    }

    public int getTouchRadius() {
        return touchRadius;
    }

}
