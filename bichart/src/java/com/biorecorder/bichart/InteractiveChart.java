package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.Orientation;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;

import java.util.ArrayList;
import java.util.List;

public class InteractiveChart implements Interactive {
    private final Chart chart;
    private List<Integer> yAxesNumbers = new ArrayList<>(2);
    private List<Integer> xAxesNumbers = new ArrayList<>(2);
    private int stack;
    private boolean released = true;

    public InteractiveChart(Chart chart) {
        this.chart = chart;
    }

    private void getPositions(int x, int y) {
        xAxesNumbers.clear();
        yAxesNumbers.clear();
        int selection = chart.getSelectedTrace();
        if (selection >= 0) {
            yAxesNumbers.add(chart.getTraceYAxisNumber(selection));
            xAxesNumbers.add(chart.getTraceXAxisNumber(selection));
        } else {
            stack = chart.getStackContaining(x, y);
            yAxesNumbers = chart.getYAxesNumbersUsedByStack(stack);
            xAxesNumbers = chart.getXAxesNumbersUsedByStack(stack);
        }
        released = false;
    }

    @Override
    public boolean centerX(int x, int y) {
        return false;
    }

    @Override
    public void release() {
        released = true;
    }

    @Override
    public boolean translateX(int x, int y, int dx) {
        if (dx == 0) {
            return false;
        }
        if(released) {
            getPositions(x, y);
        }
        for (int i = 0; i < xAxesNumbers.size(); i++) {
            chart.translateX(xAxesNumbers.get(i), dx);
        }
        return true;
    }

    @Override
    public boolean translateY(int x, int y, int dy) {
        if (dy == 0) {
            return false;
        }
        if(released) {
            getPositions(x, y);
        }
        if(stack >= 0) {
            for (int i = 0; i < yAxesNumbers.size(); i++) {
                chart.translateY(yAxesNumbers.get(i), dy);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean scaleX(int x, int y, double scaleFactor) {
        if (scaleFactor == 0) {
            return false;
        }
        if(released) {
            getPositions(x, y);
        }
        for (int i = 0; i < xAxesNumbers.size(); i++) {
            chart.zoomX(xAxesNumbers.get(i), scaleFactor, x);
        }
        return true;
    }

    @Override
    public boolean scaleY(int x, int y, double scaleFactor) {
        if(scaleFactor == 0) {
            return false;
        }
        if(released) {
            getPositions(x, y);
        }
        if(stack >= 0) {
            for (int i = 0; i < yAxesNumbers.size(); i++) {
                chart.zoomY(yAxesNumbers.get(i), scaleFactor, x);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean switchTraceSelection(int x, int y) {
        int selection = chart.getSelectedTrace();
        int newSelection = chart.getLegendButtonContaining(x, y);
        if(newSelection < 0) {
            return false;
        } else {
            if(selection == newSelection) {
                chart.removeTraceSelection();
            } else {
                chart.selectTrace(newSelection);
            }
            return true;
        }
    }

    @Override
    public boolean autoScaleX() {
        int selection = chart.getSelectedTrace();
        if(selection >= 0) {
            chart.autoScaleX(chart.getTraceXAxisNumber(selection));
        } else {
            chart.autoScaleX();
        }
        return true;
    }

    @Override
    public boolean autoScaleY() {
        int selection = chart.getSelectedTrace();
        if(selection >= 0) {
            chart.autoScaleY(chart.getTraceYAxisNumber(selection));
        } else {
            chart.autoScaleY();
        }
        return true;
    }

    @Override
    public boolean hoverOn(int x, int y) {
        TracePoint tracePoint = chart.getNearestPoint(x, y);
        if(tracePoint != null) {
            return chart.hoverOn(tracePoint.getTraceNumber(), tracePoint.getPointIndex());
        } else {
            return chart.hoverOff();
        }
    }

    @Override
    public boolean hoverOff() {
        return chart.hoverOff();
    }

    @Override
    public boolean resize(int width, int height) {
        BRectangle bounds = chart.getBounds();
        if(width != bounds.width || height != bounds.height) {
            chart.setBounds(bounds.x, bounds.y, width, height);
            return true;
        }
        return false;
    }

    @Override
    public void draw(BCanvas canvas) {
        chart.draw(canvas);
    }
}
