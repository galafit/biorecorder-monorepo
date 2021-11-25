package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.traces.TracePainter;

class Trace1 {
    private ChartData data;
    private final TracePainter tracePainter;
    private String name;
    private BColor color;
    private int[] sortedIndices;

    public Trace1(String name, ChartData data, TracePainter tracePainter, BColor traceColor) {
        this.data = data;
        this.tracePainter = tracePainter;
        this.name = name;
        this.color = traceColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int markSize() {
        return tracePainter.markSize();
    }

    public void setColor(BColor color) {
        this.color = color;
    }

    public void setData(ChartData data) {
        this.data = data;
    }

    private int nearest(double xValue) {
        if(data.size() == 0) {
            return -1;
        }
        int xColumnNumber = 0;
        // "lazy" sorting solo when "nearest" is called
        if (sortedIndices == null) {
            sortedIndices = data.sortedIndices(xColumnNumber);
        }
        int nearest = data.bisect(xColumnNumber, xValue, sortedIndices);
        if (nearest >= data.size()) {
            nearest = data.size() - 1;
        }
        if (nearest < 0) {
            nearest = 0;
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

    public int nearest(int x, int y, Scale xScale) {
        double argumentValue;
        argumentValue = xScale.invert(x);
        return nearest(argumentValue);
    }

    public BColor getColor() {
        return color;
    }

    public Range yMinMax() {
        return tracePainter.yMinMax(data);
    }

    public Range xMinMax() {
        return data.columnMinMax(0);
    }

    public String[] getTooltipInfo(int dataIndex, Scale xScale, Scale yScale) {
        return tracePainter.getTooltipInfo(data, dataIndex, xScale, yScale);
    }

    BPoint getCrosshairPoint(int dataIndex, Scale xScale, Scale yScale) {
        return tracePainter.getCrosshairPoint(data, dataIndex, xScale, yScale);
    }


    void draw(BCanvas canvas, Scale xScale, Scale yScale) {
        tracePainter.drawTrace(canvas, data, xScale, yScale, color);
    }

    public int distanceSqw(int pointIndex,  int x, int y, Scale xScale, Scale yScale) {
        BRectangle hoverRect = tracePainter.getHoverArea(data, pointIndex, xScale, yScale);
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
