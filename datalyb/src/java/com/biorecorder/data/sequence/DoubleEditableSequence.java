package com.biorecorder.data.sequence;

import com.biorecorder.data.sequence.DoubleSequence;

public interface DoubleEditableSequence extends DoubleSequence {
    void add(double value) throws UnsupportedOperationException;
    void add(double... values) throws UnsupportedOperationException;
    void set(int index, double value) throws UnsupportedOperationException;
    double[] toArray() throws UnsupportedOperationException;
}
