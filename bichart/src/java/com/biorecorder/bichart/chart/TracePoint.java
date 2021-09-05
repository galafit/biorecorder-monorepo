package com.biorecorder.bichart.chart;

/**
 * Created by galafit on 15/2/19.
 */
class TracePoint {
    private final Trace trace;
    private final int pointIndex;

    public TracePoint(Trace trace, int pointIndex) {
        this.trace = trace;
        this.pointIndex = pointIndex;
    }

    public Trace getTrace() {
        return trace;
    }

    public int getPointIndex() {
        return pointIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof TracePoint)) {
            return false;
        }

        TracePoint tcp = (TracePoint) o;
        return ((trace == tcp.trace) &&
                 (pointIndex == tcp.pointIndex));
    }
}
