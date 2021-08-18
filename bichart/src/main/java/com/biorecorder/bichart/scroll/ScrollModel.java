package com.biorecorder.bichart.scroll;

public interface ScrollModel {
    public void addListener(ScrollListener listener);

    public double getStart();

    public double getEnd();

    public double getValue();

    public double getExtent();

    public void setStartEnd(double newStart, double newEnd);

    public void setValue(double newValue);

    public void setExtent(double newExtent);

    public void setRangeProperties(double newValue, double newExtent, double newStart, double newEnd);
}
