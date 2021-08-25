package com.biorecorder.bichart.configs;


import com.biorecorder.bichart.graphics.BColor;

/**
 * Created by galafit on 1/10/17.
 */
public class ScrollConfig {
    private BColor color = BColor.GRAY;
    private BColor fillColor;
    private int touchRadius = 10; //px
    private int borderWidth = 1; // px

    public ScrollConfig() {
    }

    public ScrollConfig(ScrollConfig config) {
        color = config.color;
        fillColor = config.fillColor;
        touchRadius = config.touchRadius;
        borderWidth = config.borderWidth;
    }

    public BColor getFillColor() {
        if(fillColor != null) {
            return fillColor;
        }
        return new BColor(color.getRed(), color.getGreen(), color.getBlue(), 40);
    }

    public void setFillColor(BColor fillColor) {
        this.fillColor = fillColor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setColor(BColor color) {
        this.color = color;
    }

    public void setTouchRadius(int activeExtraSpace) {
        this.touchRadius = activeExtraSpace;
    }

    public BColor getColor() {
        return color;
    }

    public int getTouchRadius() {
        return touchRadius;
    }

}
