package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;

import java.util.ArrayList;
import java.util.List;

public class InteractiveChart implements Interactive {
    private final Chart chart;

    public InteractiveChart(Chart chart) {
        this.chart = chart;
    }

    private YAxisInfo getYPositions(int x, int y) {
        List<YAxisPosition> yPositions = new ArrayList<>(1);
        int stack;
        int selection = chart.getSelectedTrace();
        if (selection >= 0) {
            yPositions.add(chart.getTraceYPosition(selection));
            stack = chart.getTraceStack(selection);
        } else {
            stack = chart.getStackContaining(x, y);
            yPositions = chart.getYPositionsUsedByStack(stack);
        }
        return new YAxisInfo(stack, yPositions);
    }

    private List<XAxisPosition> getXPositions(int x, int y) {
        List<XAxisPosition> xPositions;
        int selection =chart.getSelectedTrace();
        if (selection >= 0) {
            xPositions = new ArrayList();
            xPositions.add(chart.getTraceXPosition(selection));
        } else {
            int stack = chart.getStackContaining(x, y);
            xPositions = chart.getXPositionsUsedByStack(stack);
        }
        return xPositions;
    }

    @Override
    public boolean translateX(int x, int y, int dx) {
        if (dx == 0) {
            return false;
        }
        List<XAxisPosition> xPositions = getXPositions(x, y);
        for (XAxisPosition xPosition : xPositions) {
            chart.translateX(xPosition, dx);
        }
        return true;
    }

    @Override
    public boolean translateY(int x, int y, int dy) {
        if (dy == 0) {
            return false;
        }
        YAxisInfo yAxisInfo = getYPositions(x, y);
        List<YAxisPosition> yPositions = yAxisInfo.getYPositions();
        int stack = yAxisInfo.getStack();
        if(stack >= 0) {
            for (YAxisPosition yPosition : yPositions) {
                chart.translateY(stack, yPosition, dy);
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
        List<XAxisPosition> xPositions = getXPositions(x, y);
        for (XAxisPosition xPosition : xPositions) {
            chart.zoomX(xPosition, scaleFactor);
        }
        return true;
    }

    @Override
    public boolean scaleY(int x, int y, double scaleFactor) {
        if(scaleFactor == 0) {
            return false;
        }
        YAxisInfo yAxisInfo = getYPositions(x, y);
        List<YAxisPosition> yPositions = yAxisInfo.getYPositions();
        int stack = yAxisInfo.getStack();
        if(stack >= 0) {
            for (YAxisPosition yPosition : yPositions) {
                chart.zoomY(stack, yPosition, scaleFactor);
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
    public boolean setScrollPosition(int x, int y) {
        return false;
    }

    @Override
    public boolean scrollContain(int x, int y) {
        return false;
    }

    @Override
    public boolean translateScroll(int dx) {
        return false;
    }

    @Override
    public boolean autoScaleX() {
        int selection = chart.getSelectedTrace();
        if(selection >= 0) {
            XAxisPosition xPosition = chart.getTraceXPosition(selection);
            chart.autoScaleX(xPosition);
        } else {
            chart.autoScaleX();
        }
        return true;
    }

    @Override
    public boolean autoScaleY() {
        int selection = chart.getSelectedTrace();
        if(selection >= 0) {
            YAxisPosition yPosition = chart.getTraceYPosition(selection);
            int stack = chart.getTraceStack(selection);
            chart.autoScaleY(stack, yPosition);
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

    private static class YAxisInfo {
        private List<YAxisPosition> yPositions;
        private int stack;

        public YAxisInfo(int stack, List<YAxisPosition> yPositions) {
            this.yPositions = yPositions;
            this.stack = stack;
        }

        public List<YAxisPosition> getYPositions() {
            return yPositions;
        }

        public int getStack() {
            return stack;
        }
    }
}
