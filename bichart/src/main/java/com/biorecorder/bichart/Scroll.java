package com.biorecorder.bichart;

import java.util.*;
import java.util.List;


/**
 * Created by galafit on 21/7/17.
 */
class Scroll {
    private double max = 1;
    private double min = 0;
    private double value = 0; // viewportPosition
    private double extent = 1; // viewportWidth
    private List<ScrollListener> eventListeners = new ArrayList<ScrollListener>();

    public Scroll(double min, double max,  double extent) {
        this.min = min;
        this.max = max;
        this.extent = extent;
        value = min;
    }

    public void addListener(ScrollListener listener) {
        eventListeners.add(listener);
    }

    private void fireListeners() {
        for (ScrollListener listener : eventListeners) {
            listener.onScrollChanged(value, extent);
        }
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public void setExtent(double newExtent) throws IllegalArgumentException {
        if(newExtent <= 0) {
            String msg = "Scroll extent = " + newExtent + " Expected >= 0";
            throw new IllegalArgumentException(msg);
        }
        double oldExtent = extent;
        extent = newExtent;
        if (extent > max - min || extent <= 0) {
            extent = max - min;
        }
        if(extent != oldExtent) {
            checkBounds();
            fireListeners();
        }
    }

    public double getExtent() {
        return extent;
    }

    public double getValue() {
        return value;
    }

    public void setMinMax(double min, double max) {
        this.min = min;
        this.max = max;
        double oldExtent = extent;
        double oldValue = value;
        if(extent > max - min) {
            extent = max - min;
        }
        checkBounds();
        if(oldExtent != extent || oldValue != value) {
            fireListeners();
        }
    }

    /**
     * @return true if value was changed and false if newValue = current scroll value
     */
    public boolean setValue(double newValue) {
        double oldValue = value;
        value = newValue;
        checkBounds();
        if (value != oldValue) {
            fireListeners();
            return true;
        }
        return false;
    }

 
    private void checkBounds() {
        if (value + extent > max) {
            value = max - extent;
        }
        if (value < min) {
            value = min;
        }
    }

}
