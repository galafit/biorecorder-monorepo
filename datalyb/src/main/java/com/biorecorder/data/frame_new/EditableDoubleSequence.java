package com.biorecorder.data.frame_new;

import com.biorecorder.data.sequence.DoubleSequence;

public interface EditableDoubleSequence extends DoubleSequence {
    void add(double value) throws UnsupportedOperationException;
    void add(double... values) throws UnsupportedOperationException;
    void set(int index, double value) throws UnsupportedOperationException;

}
