package com.biorecorder.data.datatable;

import com.biorecorder.data.sequence.DoubleEditableSequence;

public abstract class BaseDoubleEditableSequence implements DoubleEditableSequence {
    @Override
    public void add(double value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(double... values) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(int index, double value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
