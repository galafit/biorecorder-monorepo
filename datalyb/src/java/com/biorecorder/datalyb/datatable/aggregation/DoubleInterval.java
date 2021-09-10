package com.biorecorder.datalyb.datatable.aggregation;

public class DoubleInterval implements Interval {
    private final double start;
    private final double nextIntervalStart;

    public DoubleInterval(double start, double nextIntervalStart) {
        this.start = start;
        this.nextIntervalStart = nextIntervalStart;
    }

    //As we will use methods contains only on INCREASING data
    //we do only one check (value < nextIntervalStart) instead of both
    // return value >= start && value < nextIntervalStart;
    @Override
    public boolean contains(double value) {
        return value < nextIntervalStart;
    }

    @Override
    public String toString() {
        return new String("Interval: "+start + " "+ nextIntervalStart);
    }
}
