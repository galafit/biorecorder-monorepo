package com.biorecorder.bichart;

/**
 * Created by galafit on 15/2/19.
 */
class DataPainterTracePoint extends DataPainterTrace {
    protected final int pointIndex;

    public DataPainterTracePoint(DataPainter trace, int curveNumber, int pointIndex) {
        super(trace, curveNumber);
        this.pointIndex = pointIndex;
    }

    public int getPointIndex() {
        return pointIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DataPainterTracePoint)) {
            return false;
        }

        DataPainterTracePoint tcp = (DataPainterTracePoint) o;
        return ((dataPainter == tcp.dataPainter) &&
                (trace == tcp.trace) && (pointIndex == tcp.pointIndex));
    }
}
