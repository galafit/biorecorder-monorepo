package com.biorecorder.data.frame_new;

public abstract class BaseIntSequence implements EditableIntSequence {
    @Override
    public void add(int value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int... values) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(int index, int value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
