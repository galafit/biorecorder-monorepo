package com.biorecorder.datalyb.series;

/**
 * Interface that represents a set of indexed data of type long (like array)
 * that can be accessed but can not be modified
 */
public interface LongSeries {
    public int size();
    public long get(int index);
}
