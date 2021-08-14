package com.biorecorder.bichart.scroll;

import java.util.ArrayList;
import java.util.List;

/**
 * At the moment not used. Just example of standard approach
 */
public class ScrollModelDefault implements ScrollModel {
    private double max = 1;
    private double min = 0;
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
    public double getMax() {
        return max;
    }
    @Override
    public double getMin() {
        return min;
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
       setRangeProperties(value, newExtent, min, max);
    }
    @Override
    public void setMinMax(double newMin, double newMax) {
       setRangeProperties(value, extent, newMin, newMax);
    }
    @Override
    public void setValue(double newValue) {
        setRangeProperties(newValue, extent, min, max);
    }

    @Override
    public void setRangeProperties(double newValue, double newExtent, double newMin, double newMax) {
        double oldMin = min;
        double oldMax = max;
        double oldExtent = extent;
        double oldValue = value;
        min = newMin;
        max = newMax;
        if(min > max) {
          min = max;
        }
        extent = normalizeExtent(newExtent);
        value = normalizeValue(newValue, extent);
        if(oldExtent != extent || oldValue != value ||
        oldMin != min || oldMax != max) {
            fireListeners();
        }
    }

    private double normalizeExtent(double extent) {
        if(extent < 0) {
            return 0;
        }
        double maxExtent = max - min;
        if(extent > maxExtent) {
            return maxExtent;
        }
        return extent;
    }

    private double normalizeValue(double value, double extent) {
        if (value < min) {
            return min;
        }
        if (value + extent > max) {
            return max - extent;
        }
        return value;
    }
}
