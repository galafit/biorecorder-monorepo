package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;

import java.util.ArrayList;
import java.util.List;

public class Scroll1 {
    XAxisPosition NAV_DEFAULT_X_POSITION = XAxisPosition.BOTTOM;
    Chart chart;
    Chart navigator;
    XAxisPosition chartXPosition;
    private List<ScrollListener> eventListeners = new ArrayList<ScrollListener>();

    public Scroll1(Chart chart, Chart navigator, XAxisPosition chartXPosition) {
        this.chart = chart;
        this.navigator = navigator;
        this.chartXPosition = chartXPosition;
    }

    public void autoScale() {
        setExtent(chart.getBestExtent(chartXPosition));
    }

    public XAxisPosition getChartXPosition() {
        return chartXPosition;
    }

    public boolean isUsed() {
        return  chart.isXAxisUsed(chartXPosition);
    }

    public void addListener(ScrollListener listener) {
        eventListeners.add(listener);
    }

    private void fireListeners() {
        for (ScrollListener listener : eventListeners) {
            listener.onScrollChanged(getValue(), getExtent());
        }
    }

    public double getValue(){
        return chart.getXMinMax(chartXPosition).getMin();
    }

    public double getExtent(){
        return chart.getXMinMax(chartXPosition).length();
    }

    public double getMin() {
        return navigator.getXMinMax(NAV_DEFAULT_X_POSITION).getMin();
    }

    public double getMax() {
        return navigator.getXMinMax(NAV_DEFAULT_X_POSITION).getMax();
    }

    public boolean setExtent(double newExtent) throws IllegalArgumentException {
        if(newExtent <= 0) {
            String msg = "Scroll extent = " + newExtent + " Expected >= 0";
            throw new IllegalArgumentException(msg);
        }
        double max = getMax();
        double min = getMin();
        double extent = newExtent;
        if(extent > max - min) {
            extent = max - min;
        }
        double value = normalizeValue(getValue(), extent);
        if(getExtent() != extent || getValue() != value) {
            double newChartMin = value;
            double newChartMax = newChartMin + extent;
            chart.setXMinMax(chartXPosition, newChartMin, newChartMax);
            fireListeners();
            return true;
        }
        return false;
    }

    public void setMinMax(double min, double max) {
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            navigator.setXMinMax(xPosition, min, max);
        }
        double extent = getExtent();
        if(extent > max - min) {
            extent = max - min;
        }
        double value = normalizeValue(getValue(), extent);
        if(getExtent() != extent || getValue() != value) {
            double newChartMin = value;
            double newChartMax = newChartMin + extent;
            chart.setXMinMax(chartXPosition, newChartMin, newChartMax);
            fireListeners();
        }
    }

    /**
     * @return true if value was changed and false if newValue = current scroll value
     */
    public boolean setValue(double newValue) {
        double value = normalizeValue(newValue, getExtent());
        if (value != getValue()) {
            double newChartMin = value;
            double newChartMax = newChartMin + getExtent();
            chart.setXMinMax(chartXPosition, newChartMin, newChartMax);
            fireListeners();
            return true;
        }
        return false;
    }

    private double normalizeValue(double value, double extent) {
        double max = getMax();
        double min = getMin();
        if (value < min) {
            return min;
        }
        if (value + extent > max) {
           return max - extent;
        }
        return value;
    }

}
