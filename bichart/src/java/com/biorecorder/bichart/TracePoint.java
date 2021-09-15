package com.biorecorder.bichart;

/**
 * Created by galafit on 15/2/19.
 */
public class TracePoint {
    private final int traceNumber;
    private final int pointIndex;

    public TracePoint(int traceNumber, int pointIndex) {
        this.traceNumber = traceNumber;
        this.pointIndex = pointIndex;
    }

    public int getTraceNumber() {
        return traceNumber;
    }

    public int getPointIndex() {
        return pointIndex;
    }
}
