package com.biorecorder.datalyb.series;

public interface DoubleEditableSeries extends DoubleSeries {
    void add(double value) throws UnsupportedOperationException;
    void add(double... values) throws UnsupportedOperationException;
    void set(int index, double value) throws UnsupportedOperationException;
    double[] toArray() throws UnsupportedOperationException;
}
