package com.biorecorder.filters.digitalfilter;

import com.biorecorder.filters.IntCircularFifoBuffer;

public abstract class IntAbstractStatefulFilter implements IntDigitalFilter{
    private final IntCircularFifoBuffer fifoBuffer;

    public IntAbstractStatefulFilter(int bufferSize) {
        fifoBuffer = new IntCircularFifoBuffer(bufferSize);
    }

    protected int bufferSize() {
        return fifoBuffer.size();
    }

    protected int bufferMaxSize() {
        return fifoBuffer.maxSize();
    }

    protected void addToBuffer(int value) {
        fifoBuffer.add(value);
    }

    protected int getFromBuffer() {
        return fifoBuffer.get();
    }

    @Override
    public int getFilterLength() {
        return fifoBuffer.maxSize() + 1;
    }
}
