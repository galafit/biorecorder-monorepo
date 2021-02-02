package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.traces.NamedValue;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.bichart.traces.TraceType;
import com.biorecorder.data.sequence.StringSequence;
import com.sun.istack.internal.Nullable;


class DataPainter {
    private ChartData data;
    private int xIndex;
    private int yStartIndex;

    private boolean isSplit;
    private TracePainter tracePainter;
    private int traceCount;
    private String[] traceNames;
    private BColor[] traceColors;
    private boolean[] tracesVisibleMask;
    private int hiddenTraceCount = 0;
    private int[] sortedIndices;


    DataPainter(ChartData data1, TracePainter tracePainter, boolean isSplit, int xAxisIndex, int yAxesStartIndex) {
        data = data1;
        this.isSplit = isSplit;
        this.xIndex = xAxisIndex;
        this.yStartIndex = yAxesStartIndex;
        traceCount = tracePainter.traceCount(data);
        traceNames = new String[traceCount];
        traceColors = new BColor[traceCount];
        tracesVisibleMask = new boolean[traceCount];
        for (int trace = 0; trace < traceCount; trace++) {
            traceNames[trace] = tracePainter.traceName(data, trace);
            tracesVisibleMask[trace] = true;
        }

        this.tracePainter = tracePainter;
    }



    private int nearestIndex(double xValue) {
        // "lazy" sorting solo when "nearest" is called
        if (sortedIndices == null) {
            sortedIndices = data.sortedIndices(0);
        }
        int nearest = data.bisect(xValue, sortedIndices);

        if (nearest >= data.rowCount()) {
            nearest = data.rowCount() - 1;
        }

        int nearest_prev = nearest;
        if (nearest > 0) {
            nearest_prev = nearest - 1;
        }

        if (sortedIndices != null) {
            nearest = sortedIndices[nearest];
            nearest_prev = sortedIndices[nearest_prev];
        }
        if (nearest != nearest_prev) {
            if (Math.abs(data.value(nearest_prev, 0) - xValue) < Math.abs(data.value(nearest, 0) - xValue)) {
                nearest = nearest_prev;
            }
        }
        return nearest;
    }

    @Nullable
    NearestTracePoint nearest(int x, int y, int trace, Scale xScale, Scale yScale) {
        double argumentValue;
        argumentValue = xScale.invert(x);

        int pointIndex = nearestIndex(argumentValue);
        if (pointIndex < 0) {
            return null;
        }
        int distance = distanceSqw(pointIndex, trace, x, y, xScale, yScale);
        if (distance >= 0) {
            return new NearestTracePoint(new DataPainterTracePoint(this, trace, pointIndex), distance);
        } else {
            return null;
        }
    }

    @Nullable
    NearestTracePoint nearest(int x, int y, Scale xScale, Scale[] yScales) {
        double argumentValue = xScale.invert(x);
        int pointIndex = nearestIndex(argumentValue);
        if (pointIndex < 0) {
            return null;
        }
        int minDistance = -1;
        int closestTrace = -1;
        for (int trace = 0; trace < traceCount; trace++) {
            if(tracesVisibleMask[trace]) {
                int distance = distanceSqw(pointIndex, trace, x, y, xScale, yScales[trace]);
                if (distance == 0) {
                    return new NearestTracePoint(new DataPainterTracePoint(this, trace, pointIndex), 0);
                } else if (distance > 0) {
                    if (minDistance < 0 || minDistance > distance) {
                        minDistance = distance;
                        closestTrace = trace;
                    }
                }
            }
        }
        if (closestTrace >= 0) {
            return new NearestTracePoint(new DataPainterTracePoint(this, closestTrace, pointIndex), minDistance);
        }
        return null;
    }

    double getBestExtent(int drawingAreaWidth) {
        int markSize = tracePainter.markWidth();
        if (data.rowCount() > 1) {
            if (markSize <= 0) {
                markSize = 1;
            }
            double traceExtent = getDataAvgStep(data) * drawingAreaWidth / markSize;
            if(tracePainter.traceType() == TraceType.SCATTER) {
                traceExtent = Math.sqrt(traceExtent);
            }
            return traceExtent;
        }
        return -1;
    }

    double getDataAvgStep(ChartData data) {
        int dataSize = data.rowCount();
        return (data.value(dataSize - 1, 0) - data.value(0, 0)) / (dataSize - 1);
    }


    public @Nullable StringSequence getXLabels() {
        if (!data.isNumberColumn(0)) {
            return new StringSequence() {
                @Override
                public int size() {
                    return data.rowCount();
                }

                @Override
                public String get(int index) {
                    return data.label(index, 0);
                }
            };
        }
        return null;
    }

    void hideTrace(int trace) {
        tracesVisibleMask[trace] = false;
        hiddenTraceCount++;
    }

    boolean isSplit() {
        return isSplit;
    }

    int getYStartIndex() {
        return yStartIndex;
    }

    void setYStartIndex(int yScaleStartIndex) {
        this.yStartIndex = yScaleStartIndex;
    }

    int getXIndex() {
        return xIndex;
    }

    private void checkTraceNumber(int traceNumber) {
        if (traceNumber >= traceCount()) {
            String errMsg = "Trace = " + traceNumber + " Number of traces: " + traceCount();
            throw new IllegalArgumentException(errMsg);
        }
    }

    Range traceYMinMax(int trace) {
        checkTraceNumber(trace);
        return tracePainter.traceYMinMax(data, trace);
    }

    Range xMinMax() {
        return tracePainter.xMinMax(data);
    }

    int traceCount() {
        return traceCount - hiddenTraceCount;
    }

    Tooltip createTooltip(TooltipConfig tooltipConfig, int hoverPointIndex, int hoverTrace, Scale xScale, Scale[] yScales) {
        int tooltipYPosition = 0;
        double xValue = data.value(hoverPointIndex, 0);
        int xPosition = (int)xScale.scale(xValue);
        Tooltip tooltip = new Tooltip(tooltipConfig, xPosition, tooltipYPosition);
        tooltip.setHeader(null, null, xScale.formatDomainValue(xValue));
        tooltip.addXCrosshair(xIndex, xPosition);
        int traceStart;
        int traceEnd;
        if (tooltipConfig.isShared()) { // all traces
            traceStart = 0;
            traceEnd = traceCount() - 1;
        } else { // only hover trace
            traceStart = hoverTrace;
            traceEnd = hoverTrace;
        }
        for (int trace = traceStart; trace <= traceEnd; trace++) {
            Scale yScale = yScales[trace];
            NamedValue[] traceValues = tracePainter.tracePointValues(data, hoverPointIndex, trace, xScale, yScale);

            if (traceValues.length == 1) {
                tooltip.addLine(getTraceColor(trace), getTraceName(trace), traceValues[0].getValue());
            } else {
                tooltip.addLine(getTraceColor(trace), getTraceName(trace), "");
                for (NamedValue traceValue : traceValues) {
                    tooltip.addLine(null, traceValue.getValueName(), traceValue.getValue());
                }
            }
            BPoint crosshairPosition = tracePainter.tracePointCrosshair(data, hoverPointIndex, trace, xScale, yScale);
            tooltip.addYCrosshair(yStartIndex + trace, crosshairPosition.getY());
        }
        return tooltip;
    }

    void setTraceName(int trace, String name) {
        traceNames[trace] = name;
    }

    void setTraceColor(int trace, BColor color) {
        checkTraceNumber(trace);
        traceColors[trace] = color;
    }

    BColor getTraceColor(int trace) {
        checkTraceNumber(trace);
        return traceColors[trace];
    }

    String getTraceName(int trace) {
        checkTraceNumber(trace);
        return traceNames[trace];
    }

    void drawTrace(BCanvas canvas, int trace, Scale xScale, Scale yScale) {
        if(tracesVisibleMask[trace]) {
            tracePainter.drawTrace(canvas, data, trace, getTraceColor(trace), traceCount(), isSplit, xScale, yScale);
        }
    }

    private int distanceSqw(int pointIndex, int trace, int x, int y, Scale xScale, Scale yScale) {
        checkTraceNumber(trace);
        BRectangle hoverRect = tracePainter.tracePointHoverArea(data, pointIndex, trace, xScale, yScale);
        if (hoverRect.width > 0 && hoverRect.height > 0) {
            if (hoverRect.contains(x, y)) {
                return 0;
            } else {
                return -1;
            }
        } else if (hoverRect.width > 0) {
            if (hoverRect.containsX(x)) {
                return 0;
            } else {
                return -1;
            }
        } else if (hoverRect.height > 0) {
            if (hoverRect.containsY(y)) {
                return 0;
            } else {
                return -1;
            }
        }

        int dy = hoverRect.y - y;
        int dx = hoverRect.x - x;
        return dy * dy + dx * dx;
    }
}
