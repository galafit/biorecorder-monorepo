package com.biorecorder.data.frame;

/**
 * Created by galafit on 1/5/19.
 */
public interface Interval {
    boolean contains(byte value);
    boolean contains(short value);
    boolean contains(int value);
    boolean contains(long value);
    boolean contains(float value);
    boolean contains(double value);
}
