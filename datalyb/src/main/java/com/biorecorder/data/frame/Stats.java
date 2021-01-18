package com.biorecorder.data.frame;

/**
 * Created by galafit on 10/3/19.
 */
public interface Stats {
    double min();

    double max();

    boolean isIncreasing();

    boolean isDecreasing();
}
