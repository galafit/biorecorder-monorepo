package com.biorecorder.bichart;

/**
 * Created by galafit on 26/5/19.
 */
class NearestTracePoint {
    private DataPainterTracePoint tracePoint;
    private int distanceSqw;

    public NearestTracePoint(DataPainterTracePoint curvePoint, int distanceSqw) {
        this.tracePoint = curvePoint;
        this.distanceSqw = distanceSqw;
    }

    public DataPainterTracePoint getTracePoint() {
        return tracePoint;
    }

    public int getDistanceSqw() {
        return distanceSqw;
    }
}
