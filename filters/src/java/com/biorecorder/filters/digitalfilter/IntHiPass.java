package com.biorecorder.filters.digitalfilter;

public class IntHiPass extends IntAbstractStatefulFilter {
    private long sum;

    public IntHiPass(int bufferSize) {
        super(bufferSize);
    }

    @Override
    public int filteredValue(int value) {
        sum += value;
        if(bufferSize() == bufferMaxSize()) {
            sum -= getFromBuffer();
        }
        addToBuffer(value);

        int avg = (int) (sum / bufferSize());
        return value - avg;
    }
}
