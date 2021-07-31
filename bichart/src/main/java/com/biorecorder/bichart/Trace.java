package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.bichart.traces.TraceType;

public class Trace {
    private ChartData data;
    private final TracePainter tracePainter;
    private final XAxisPosition xAxisPosition;
    private final YAxisPosition yAxisPosition;
    private final AxisWrapper xAxis;
    private final AxisWrapper yAxis;
    private String name;
    private BColor color;

    private int[] sortedIndices;
    private Range xMinMax;
    private Range yMinMax;

    public Trace(String name, ChartData data, TracePainter tracePainter, XAxisPosition xAxisPosition, YAxisPosition yAxisPosition, AxisWrapper xAxis, AxisWrapper yAxis, BColor traceColor) {
        this.data = data;
        this.tracePainter = tracePainter;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.name = name;
        this.color = traceColor;
        this.xAxisPosition = xAxisPosition;
        this.yAxisPosition = yAxisPosition;
        xMinMax = tracePainter.xMinMax(data);
        yMinMax = tracePainter.yMinMax(data);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(BColor color) {
        this.color = color;
    }

    public void setData(ChartData data) {
        this.data = data;
        xMinMax = tracePainter.xMinMax(data);
        yMinMax = tracePainter.yMinMax(data);
    }

    public AxisWrapper getXAxis() {
        return xAxis;
    }

    public AxisWrapper getYAxis() {
        return yAxis;
    }

    public XAxisPosition getXAxisPosition() {
        return xAxisPosition;
    }

    public YAxisPosition getYAxisPosition() {
        return yAxisPosition;
    }

    private int nearest(double xValue) {
        // "lazy" sorting solo when "nearest" is called
        if (sortedIndices == null) {
            sortedIndices = data.sortedIndices();
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

   public int nearest(int x, int y) {
        double argumentValue;
        argumentValue = xAxis.invert(x);
        return nearest(argumentValue);
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

    public BColor getColor() {
        return color;
    }

    public Range yMinMax() {
        return yMinMax;
    }

    public Range xMinMax() {
        return xMinMax;
    }

    public String[] getTooltipInfo(int dataIndex) {
        return tracePainter.getTooltipInfo(data, dataIndex, xAxis.getScale(), yAxis.getScale());
    }

    BPoint getCrosshairPoint(int dataIndex) {
        return tracePainter.getCrosshairPoint(data, dataIndex, xAxis.getScale(), yAxis.getScale());
    }


    void draw(BCanvas canvas) {
        tracePainter.drawTrace(canvas, data, xAxis.getScale(), yAxis.getScale(), color);
    }

   public int distanceSqw(int pointIndex,  int x, int y) {
        BRectangle hoverRect = tracePainter.getHoverArea(data, pointIndex, xAxis.getScale(), yAxis.getScale());
        if (hoverRect.width > 0 && hoverRect.height > 0) {
            if (hoverRect.contain(x, y)) {
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

        int dy = hoverRect.y + hoverRect.height/2 - y;
        int dx = hoverRect.x + hoverRect.width/2 - x;
        return dy * dy + dx * dx;
    }
}
