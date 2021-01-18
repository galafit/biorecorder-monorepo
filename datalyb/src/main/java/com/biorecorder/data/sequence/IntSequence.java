package com.biorecorder.data.sequence;

/**
 * Interface that represents a set of indexed data of type int (like array)
 * that can be accessed but can not be modified
 */
public interface IntSequence {
    public int size();
    public int get(int index);
}
