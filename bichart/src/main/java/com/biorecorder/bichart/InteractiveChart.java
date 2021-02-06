package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.RenderContext;
import com.sun.istack.internal.Nullable;

/**
 * Created by galafit on 21/9/18.
 */
public class InteractiveChart implements InteractiveDrawable {
    private  final Chart chart;

    public InteractiveChart(Chart chart) {
        this.chart = chart;
    }

    @Override
    public void onResize(int width, int height) {
        chart.setSize(width, height);
    }

    @Override
    public boolean onTap(int x, int y) {
        return chart.selectTrace(x, y);
    }

    @Override
    public boolean onDoubleTap(int x, int y) {
        if(chart.isTraceSelected()) {
            chart.autoScaleX(chart.getSelectedTraceX());
            chart.autoScaleY(chart.getSelectedTraceStack(), chart.getSelectedTraceY());
        } else {
            XAxisPosition[] xAxisPositions = chart.getXAxes();
            for (int i = 0; i < xAxisPositions.length; i++) {
                chart.autoScaleX(xAxisPositions[i]);
            }
            for (int stack = 0; stack < chart.stackCount(); stack++) {
                YAxisPosition[] yAxisPositions = chart.getYAxes(stack);
                for (int i = 0; i < yAxisPositions.length; i++) {
                    chart.autoScaleY(stack, yAxisPositions[i]);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onTapUp(int x, int y) {
        return chart.hoverOff();
    }

    @Override
    public boolean onLongPress(int x, int y) {
        if(chart.traceCount() == 0) {
            return false;
        }

       return chart.hoverOn(x, y);
    }

    @Override
    public boolean onScaleX(@Nullable BPoint startPoint, double scaleFactor) {
        if(scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }

        XAxisPosition[] xAxisPositions = new XAxisPosition[0];
        if(chart.isTraceSelected()) {
            XAxisPosition[] xAxisPositions1 = {chart.getSelectedTraceX()};
            xAxisPositions = xAxisPositions1;
        } else if(startPoint != null) {
            XAxisPosition xPosition = chart.getXAxis(startPoint);
            if(xPosition != null) {
                XAxisPosition[] xAxisPositions1 = {xPosition};
                xAxisPositions = xAxisPositions1;
            }
        } else {
           xAxisPositions = chart.getXAxes();
        }

        if(xAxisPositions.length > 0) {
            for (int i = 0; i < xAxisPositions.length; i++) {
                chart.zoomX(xAxisPositions[i], scaleFactor);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onScaleY(@Nullable BPoint startPoint, double scaleFactor) {
        if(scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }

        if(chart.isTraceSelected()) {
            chart.zoomY(chart.getSelectedTraceStack(), chart.getSelectedTraceY(), scaleFactor);
            return true;
        }

        if(startPoint != null) {
            int stack = chart.getStack(startPoint);
            if(stack >= 0) {
                YAxisPosition yAxisPosition = chart.getYAxis(stack, startPoint);
                if(yAxisPosition != null) {
                    chart.zoomY(stack, yAxisPosition, scaleFactor);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onScrollX(@Nullable BPoint startPoint, int dx) {
        if(dx == 0) {
            return false;
        }

        XAxisPosition[] xAxisPositions = new XAxisPosition[0];
        if(chart.isTraceSelected()) {
            XAxisPosition[] xAxisPositions1 = {chart.getSelectedTraceX()};
            xAxisPositions = xAxisPositions1;
        } else if(startPoint != null) {
            XAxisPosition xPosition = chart.getXAxis(startPoint);
            if(xPosition != null) {
                XAxisPosition[] xAxisPositions1 = {xPosition};
                xAxisPositions = xAxisPositions1;
            }
        } else {
            xAxisPositions = chart.getXAxes();
        }

        if(xAxisPositions.length > 0) {
            for (int i = 0; i < xAxisPositions.length; i++) {
                chart.translateX(xAxisPositions[i], dx);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onScrollY(@Nullable BPoint startPoint, int dy) {
        if(dy == 0) {
            return false;
        }
        if(chart.isTraceSelected()) {
            chart.translateY(chart.getSelectedTraceStack(), chart.getSelectedTraceY(), dy);
            return true;
        }

        if(startPoint != null) {
            int stack = chart.getStack(startPoint);
            if(stack >= 0) {
                YAxisPosition yAxisPosition = chart.getYAxis(stack, startPoint);
                if(yAxisPosition != null) {
                    chart.translateY(stack, yAxisPosition, dy);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void draw(BCanvas canvas) {
        chart.draw(canvas);

    }

    @Override
    public boolean update(RenderContext renderContext) {
       chart.revalidate(renderContext);
        return true;
    }
}
