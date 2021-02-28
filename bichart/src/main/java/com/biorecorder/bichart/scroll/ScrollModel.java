package com.biorecorder.bichart.scroll;

public interface ScrollModel {
    public void addListener(ScrollListener listener);

    public double getMax();

    public double getMin();

    public double getValue();

    public double getExtent();

    public void setMinMax(double newMin, double newMax);

    public void setValue(double newValue);

    public void setExtent(double newExtent);

    public void setRangeProperties(double newValue, double newExtent, double newMin, double newMax);
}
