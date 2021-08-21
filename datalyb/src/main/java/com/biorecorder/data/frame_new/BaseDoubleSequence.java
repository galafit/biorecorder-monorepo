package com.biorecorder.data.frame_new;

public abstract class BaseDoubleSequence implements EditableDoubleSequence {
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
