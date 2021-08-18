package com.biorecorder.bichart.scroll;

import com.biorecorder.bichart.ScrollConfig;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;
import com.biorecorder.bichart.graphics.DashStyle;
import com.biorecorder.bichart.graphics.Range;

import java.util.ArrayList;
import java.util.List;

public class Scroll1 {
    private ScrollConfig config;
    private double startValue;
    private double endValue;
    private ScrollScale scale;
    private List<ScrollListener> eventListeners = new ArrayList<ScrollListener>();

    public Scroll1(ScrollConfig config, ScrollScale scrollScale, Range scrollRange) {
        this.config = config;
        this.scale = scrollScale;
        startValue = scrollRange.getMin();
        endValue = scrollRange.getMax();
    }

    public void addListener(ScrollListener listener) {
        eventListeners.add(listener);
    }

    private void fireListeners() {
        for (ScrollListener listener : eventListeners) {
            listener.onScrollChanged(startValue, endValue);
        }
    }

    private void normalizeAndSetScrollRange(double scrollStart, double scrollWidth) {
        Range minMax = scale.getMinMax();
        double start = scale.valueToPosition(minMax.getMin());
        double end = scale.valueToPosition(minMax.getMax());
        if(scrollWidth > end - start) {
            scrollWidth = end - start;
        }
        if(scrollStart < start) {
            scrollStart = start;
        }
        if(scrollStart + scrollWidth > end) {
            scrollStart = end - scrollWidth;
        }
        double oldStartValue = startValue;
        double oldEndValue = endValue;
        startValue = scale.positionToValue(scrollStart);
        endValue = scale.positionToValue(scrollStart + scrollWidth);
        if(startValue != oldStartValue || endValue != oldEndValue) {
            fireListeners();
        }
    }

    public void moveToValue(double value) {
        double width = scale.valueToPosition(endValue) - scale.valueToPosition(startValue);
        double scrollStart = scale.valueToPosition(value);
        normalizeAndSetScrollRange(scrollStart, width);
    }

    public void moveToPosition(double position) {
        double width = scale.valueToPosition(endValue) - scale.valueToPosition(startValue);
        normalizeAndSetScrollRange(position, width);
    }

    public void movePosition(double dx) {
        double position = scale.valueToPosition(startValue);
        moveToPosition(position + dx);
    }

    public void zoomRange(double zoomFactor) {
        double width = scale.valueToPosition(endValue) - scale.valueToPosition(startValue);
        double scrollStart = scale.valueToPosition(startValue);
        normalizeAndSetScrollRange(scrollStart, width * zoomFactor);
    }

    public double scrollRatio() {
        Range minMax = scale.getMinMax();
        double start = scale.valueToPosition(minMax.getMin());
        double end = scale.valueToPosition(minMax.getMax());
        double scrollStart = scale.valueToPosition(startValue);
        double scrollEnd = scale.valueToPosition(endValue);

        return (scrollEnd - scrollStart) / (end - start);
    }

    public void setScrollRange(double startValue, double endValue) {
        double scrollStart = scale.valueToPosition(startValue);
        double scrollEnd = scale.valueToPosition(endValue);
        normalizeAndSetScrollRange(scrollStart, scrollEnd - scrollStart);
    }

    public Range getTouchRange() {
        int touchRadius = config.getTouchRadius();
        double scrollStart = scale.valueToPosition(startValue);
        double scrollEnd = scale.valueToPosition(endValue);
        double scrollWidth = scrollEnd - scrollStart;
        double delta = touchRadius - scrollWidth/2;
        if(delta > 0) {
            scrollStart = scrollStart - delta;
            scrollWidth = 2 * touchRadius;
        }
        return new Range(scrollStart, scrollStart + scrollWidth);
    }

    public void draw(BCanvas canvas, BRectangle area) {
        int scrollY = area.y;
        int height = area.height;
        double scrollStart = scale.valueToPosition(startValue);
        double scrollEnd = scale.valueToPosition(endValue);
        int scrollWidth = (int)(scrollEnd - scrollStart);
        if(scrollWidth < 1) {
            scrollWidth = 1;
        }
        Range touchRange = getTouchRange();
        int touchWidth = (int) touchRange.length();

        canvas.setColor(config.getFillColor());
        if (touchRange.length() != scrollWidth) {
            canvas.fillRect((int)touchRange.getMin(), scrollY, touchWidth, height);
        } else {
            canvas.fillRect((int)scrollStart, scrollY, scrollWidth, height);
        }
        canvas.setColor(config.getColor());
        canvas.setStroke(config.getBorderWidth(), DashStyle.SOLID);
        canvas.drawRect((int)scrollStart, scrollY, scrollWidth, height);
    }
}
