package com.biorecorder.data.frame_new;

import com.biorecorder.data.sequence.IntSequence;

public interface EditableIntSequence extends IntSequence {
    void add(int value) throws UnsupportedOperationException;
    void add(int... values) throws UnsupportedOperationException;
    void set(int index, int value) throws UnsupportedOperationException;
}
