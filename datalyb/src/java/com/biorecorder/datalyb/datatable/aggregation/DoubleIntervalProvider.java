package com.biorecorder.datalyb.datatable.aggregation;

public class DoubleIntervalProvider implements IntervalProvider {
    double interval;
    double currentIntervalStart;

    public DoubleIntervalProvider(double interval) {
        this.interval = interval;
        currentIntervalStart = 0;
    }

    @Override
    public Interval getContaining(double value) {
        currentIntervalStart =  (int)(value / interval) * interval;
        if (currentIntervalStart > value) {
            currentIntervalStart -= interval;
        }
        return new DoubleInterval(currentIntervalStart, (currentIntervalStart + interval));
    }

    @Override
    public Interval getNext() {
        currentIntervalStart += interval;
        return new DoubleInterval(currentIntervalStart, (currentIntervalStart + interval));

    }

    @Override
    public Interval getPrevious() {
        currentIntervalStart -= interval;
        return new DoubleInterval(currentIntervalStart, (currentIntervalStart + interval));
    }
}
