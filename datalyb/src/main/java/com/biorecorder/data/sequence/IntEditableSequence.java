package com.biorecorder.data.sequence;


public interface IntEditableSequence extends IntSequence {
    void add(int value) throws UnsupportedOperationException;
    void add(int... values) throws UnsupportedOperationException;
    void set(int index, int value) throws UnsupportedOperationException;
    int[] toArray() throws UnsupportedOperationException;
}
