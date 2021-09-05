package com.biorecorder.bichart.chart;

import com.biorecorder.bichart.Interactive;
import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BPoint;

public class InteractiveChart1 implements Interactive {
    private final Chart chart;

    public InteractiveChart1(Chart chart) {
        this.chart = chart;
    }

    @Override
    public boolean translateX(int x, int y, int dx) {
        if (dx == 0) {
            return false;
        }
        if (chart.isTraceSelected()) {
            return chart.translateX(chart.getSelectedTraceX(), dx);
        } else {
            XAxisPosition xPosition = chart.getXAxisPosition(new BPoint(x, y));
            return chart.translateX(xPosition, dx);
        }
    }

    @Override
    public boolean translateY(int x, int y, int dy) {
        return false;
    }

    @Override
    public boolean scaleX(int x, int y, double scaleFactor) {
        return false;
    }

    @Override
    public boolean scaleY(int x, int y, double scaleFactor) {
        return false;
    }

    @Override
    public boolean switchTraceSelection(int x, int y) {
        return false;
    }

    @Override
    public boolean setScrollPosition(int x, int y) {
        return false;
    }

    @Override
    public boolean translateScroll(int x, int y, int dx) {
        return false;
    }

    @Override
    public boolean translateScroll(int dx) {
        return false;
    }

    @Override
    public boolean autoScale() {
        return false;
    }

    @Override
    public boolean hoverOn(int x, int y) {
        return false;
    }

    @Override
    public boolean hoverOff() {
        return false;
    }

    @Override
    public boolean resize(int width, int height) {
        return false;
    }

    @Override
    public void draw(BCanvas canvas) {

    }
}
