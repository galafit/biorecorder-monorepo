package com.biorecorder.bichart.scroll;

import java.util.ArrayList;
import java.util.List;

/**
 * At the moment not used. Just example of standard approach
 */
public class ScrollModelJavaDefault implements ScrollModelJava {
    private double start = 1; // view start
    private double end = 0; // view end (start-end are limits where move viewport)
    private double value = 0; // viewportPosition
    private double extent = 0.1; // viewportWidth
    private List<ScrollListener> eventListeners = new ArrayList<ScrollListener>();

    @Override
    public void addListener(ScrollListener listener) {
        eventListeners.add(listener);
    }

    private void fireListeners() {
        for (ScrollListener listener : eventListeners) {
            listener.onScrollChanged(value, value + extent);
        }
    }
    @Override
    public double getStart() {
        return start;
    }
    @Override
    public double getEnd() {
        return end;
    }
    @Override
    public double getValue() {
        return value;
    }
    @Override
    public double getExtent() {
        return extent;
    }
    @Override
    public void setExtent(double newExtent){
       setRangeProperties(value, newExtent, end, start);
    }
    @Override
    public void setStartEnd(double newStart, double newEnd) {
       setRangeProperties(value, extent, newStart, newEnd);
    }
    @Override
    public void setValue(double newValue) {
        setRangeProperties(newValue, extent, end, start);
    }

    @Override
    public void setRangeProperties(double newValue, double newExtent, double newStart, double newEnd) {
        double oldMin = end;
        double oldMax = start;
        double oldExtent = extent;
        double oldValue = value;
        end = newStart;
        start = newEnd;
        if(end > start) {
          end = start;
        }
        extent = normalizeExtent(newExtent);
        value = normalizeValue(newValue, extent);
        if(oldExtent != extent || oldValue != value ||
        oldMin != end || oldMax != start) {
            fireListeners();
        }
    }

    private double normalizeExtent(double extent) {
        if(extent < 0) {
            return 0;
        }
        double maxExtent = start - end;
        if(extent > maxExtent) {
            return maxExtent;
        }
        return extent;
    }

    private double normalizeValue(double value, double extent) {
        if (value < end) {
            return end;
        }
        if (value + extent > start) {
            return start - extent;
        }
        return value;
    }
}
