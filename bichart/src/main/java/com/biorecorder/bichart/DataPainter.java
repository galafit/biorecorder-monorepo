package com.biorecorder.bichart;

import com.biorecorder.bichart.data.ChartData;
import com.biorecorder.bichart.data.DataProcessingConfig;
import com.biorecorder.bichart.data.GroupApproximation;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.traces.NamedValue;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.bichart.traces.TraceType;
import com.biorecorder.data.sequence.StringSequence;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO implement XY search nearest point (QuadTree)
 */
class DataPainter {
    private int xIndex;
    private int yStartIndex;
    private GroupApproximation[] defaultApproximations = {GroupApproximation.OPEN, GroupApproximation.AVERAGE};

    private boolean isSplit;
    private DataManager dataManager;
    private TracePainter tracePainter;
    private NearestSearchType nearestSearchType;
    private int traceCount;
    private String[] traceNames;
    private BColor[] traceColors;
    private boolean[] tracesVisibleMask;
    private int hiddenTraceCount = 0;

    DataPainter(ChartData data1, TracePainter tracePainter, boolean isSplit, DataProcessingConfig dataProcessingConfig1, int xAxisIndex, int yAxesStartIndex) {
        ChartData data = data1.view(0);
        DataProcessingConfig dataProcessingConfig = new DataProcessingConfig(dataProcessingConfig1);

        if (tracePainter.traceType() == TraceType.LINEAR) {
            nearestSearchType = NearestSearchType.X;
        } else {
            nearestSearchType = NearestSearchType.XY;
        }
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

        // set approximations
        GroupApproximation[] approximations = tracePainter.groupApproximations();
        if (approximations == null || approximations.length > 0) {
            approximations = defaultApproximations;
        }
        if (approximations.length < data.columnCount()) {
            // we need it in the case of RANGE and OHLC approximations
            // if data is already grouped and so is multi-dimensional
            List<GroupApproximation> deployedApproximations = new ArrayList<>();
            for (int i = 0; i < approximations.length; i++) {
                GroupApproximation[] deployedApprox_i = approximations[i].getAsArray();
                for (GroupApproximation approx : deployedApprox_i) {
                    deployedApproximations.add(approx);
                }
            }
        }
        for (int column = 0; column < data.columnCount(); column++) {
            if (data.getColumnGroupApproximation(column) == null) {
                data.setColumnGroupApproximation(column, approximations[Math.min(column, approximations.length - 1)]);
            }
        }
        this.tracePainter = tracePainter;
        dataManager = new DataManager(data, dataProcessingConfig);
    }

    public @Nullable StringSequence getXLabels() {
        ChartData data = dataManager.getData();
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

    void appendData() {
        dataManager.appendData();
    }

    double getBestExtent(int drawingAreaWidth) {
        return dataManager.getBestExtent(drawingAreaWidth, tracePainter.markWidth(), tracePainter.traceType());
    }

    Range traceYMinMax(int trace, Scale xScale) {
        checkTraceNumber(trace);
        return tracePainter.traceYMinMax(getProcessedData(xScale), trace);
    }

    Range xMinMax() {
        return tracePainter.xMinMax(dataManager.getData());
    }

    int traceCount() {
        return traceCount - hiddenTraceCount;
    }


    @Nullable
    NearestTracePoint nearest(int x, int y, int trace, Scale xScale, Scale yScale) {
        double argumentValue;
        argumentValue = xScale.invert(x);

        int pointIndex = dataManager.nearest(argumentValue);
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
        int pointIndex = dataManager.nearest(argumentValue);
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

    Tooltip createTooltip(TooltipConfig tooltipConfig, int hoverPointIndex, int hoverTrace, Scale xScale, Scale[] yScales) {
        int tooltipYPosition = 0;
        double xValue = getProcessedData(xScale).value(hoverPointIndex, 0);
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
            NamedValue[] traceValues = tracePainter.tracePointValues(getProcessedData(xScale), hoverPointIndex, trace, xScale, yScale);

            if (traceValues.length == 1) {
                tooltip.addLine(getTraceColor(trace), getTraceName(trace), traceValues[0].getValue());
            } else {
                tooltip.addLine(getTraceColor(trace), getTraceName(trace), "");
                for (NamedValue traceValue : traceValues) {
                    tooltip.addLine(null, traceValue.getValueName(), traceValue.getValue());
                }
            }
            BPoint crosshairPosition = tracePainter.tracePointCrosshair(getProcessedData(xScale), hoverPointIndex, trace, xScale, yScale);
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
            tracePainter.drawTrace(canvas, getProcessedData(xScale), trace, getTraceColor(trace), traceCount(), isSplit, xScale, yScale);
        }
    }

    private ChartData getProcessedData(Scale xScale) {
        if(tracePainter.traceType() == TraceType.SCATTER) {
            dataManager.getData();
        }
        return dataManager.getProcessedData(xScale, tracePainter.markWidth());
    }

    private int distanceSqw(int pointIndex, int trace, int x, int y, Scale xScale, Scale yScale) {
        checkTraceNumber(trace);
        BRectangle hoverRect = tracePainter.tracePointHoverArea(getProcessedData(xScale), pointIndex, trace, xScale, yScale);
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
