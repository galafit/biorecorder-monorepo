package com.biorecorder.data.sequence;

/**
 * Interface that represents a set of indexed data of type double (like array)
 * that can be accessed but can not be modified
 */
public interface DoubleSequence {
    public int size();
    public double get(int index);
}
