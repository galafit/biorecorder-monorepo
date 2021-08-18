package com.biorecorder.bichart.scroll;

import com.biorecorder.bichart.graphics.Range;

public interface ScrollScale {
    double positionToValue(double x);
    double valueToPosition(double value);
    Range  getMinMax();
}
