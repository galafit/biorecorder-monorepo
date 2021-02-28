package com.biorecorder.bichart.scroll;

public interface ScrollScale {
    double positionToValue(double x);
    double valueToPosition(double value);
}
