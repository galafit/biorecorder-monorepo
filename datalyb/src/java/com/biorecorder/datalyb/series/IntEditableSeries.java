package com.biorecorder.datalyb.series;


public interface IntEditableSeries extends IntSeries {
    void add(int value) throws UnsupportedOperationException;
    void add(int... values) throws UnsupportedOperationException;
    void set(int index, int value) throws UnsupportedOperationException;
    int[] toArray() throws UnsupportedOperationException;
}
