package com.biorecorder.data.datatable.aggregation;

/**
 * Created by galafit on 28/4/19.
 */
public interface IntervalProvider {
    Interval getContaining(double value);
    Interval getNext();
    Interval getPrevious();
}
