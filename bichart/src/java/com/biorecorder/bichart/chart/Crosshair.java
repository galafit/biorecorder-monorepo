package com.biorecorder.bichart.chart;

import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.BRectangle;

class Crosshair {
    private final AxisWrapper xAxis;
    private final AxisWrapper yAxis;
    private final BPoint position;

    public Crosshair(BPoint position, AxisWrapper xAxis, AxisWrapper yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.position = position;
    }

    public AxisWrapper getXAxis() {
        return xAxis;
    }

    public AxisWrapper getYAxis() {
        return yAxis;
    }

    public BPoint getPosition() {
        return position;
    }

    public void draw(BCanvas canvas, BRectangle area) {
        xAxis.drawCrosshair(canvas, area, position.getX());
        yAxis.drawCrosshair(canvas, area, position.getY());
    }
}