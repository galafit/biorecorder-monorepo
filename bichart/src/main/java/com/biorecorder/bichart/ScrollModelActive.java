package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.scroll.ScrollModel;

import java.util.ArrayList;
import java.util.List;

public class ScrollModelActive implements ScrollModel {
    XAxisPosition NAV_X_POSITION = XAxisPosition.BOTTOM;
    Chart chart;
    Chart navigator;
    XAxisPosition chartXPosition;
    private List<ScrollListener> eventListeners = new ArrayList<ScrollListener>();

    public ScrollModelActive(Chart chart, Chart navigator, XAxisPosition chartXPosition) {
        this.chart = chart;
        this.navigator = navigator;
        this.chartXPosition = chartXPosition;
    }

    @Override
    public void addListener(ScrollListener listener) {
        eventListeners.add(listener);
    }

    private void fireListeners() {
        for (ScrollListener listener : eventListeners) {
            listener.onScrollChanged(getValue(), getValue() + getExtent());
        }
    }

    @Override
    public double getValue() {
        return chart.getXMinMax(chartXPosition).getMin();
    }

    @Override
    public double getExtent() {
        return chart.getXMinMax(chartXPosition).length();
    }

    @Override
    public double getEnd() {
        return navigator.getXMinMax(NAV_X_POSITION).getMin();
    }

    @Override
    public double getStart() {
        return navigator.getXMinMax(NAV_X_POSITION).getMax();
    }

    @Override
    public void setExtent(double newExtent) {
        setRangeProperties(getValue(), newExtent, getEnd(), getStart());
    }

    @Override
    public void setStartEnd(double newStart, double newEnd) {
        setRangeProperties(getValue(), getExtent(), newStart, newEnd);
    }
    @Override
    public void setValue(double newValue) {
        setRangeProperties(newValue, getExtent(), getEnd(), getStart());
    }

    @Override
    public void setRangeProperties(double newValue, double newExtent, double newStart, double newEnd) {
        double oldMin = getEnd();
        double oldMax = getStart();
        double oldExtent = getExtent();
        double oldValue = getValue();
        double min = newStart;
        double max = newEnd;
        if(min > max) {
            min = max;
        }
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            navigator.setXMinMax(xPosition, min, max);
        }
        double extent = normalizeExtent(newExtent);
        double value = normalizeValue(newValue, extent);
        chart.setXMinMax(chartXPosition, value, value + extent);
        if(oldExtent != extent || oldValue != value ||
                oldMin != min || oldMax != max) {
            fireListeners();
        }
    }

    private double normalizeExtent(double extent) {
        if(extent < 0) {
            return 0;
        }
        double maxExtent = getStart() - getEnd();
        if(extent > maxExtent) {
            return maxExtent;
        }
        return extent;
    }

    private double normalizeValue(double value, double extent) {
        double max = getStart();
        double min = getEnd();
        if (value < min) {
            return min;
        }
        if (value + extent > max) {
            return max - extent;
        }
        return value;
    }

}

