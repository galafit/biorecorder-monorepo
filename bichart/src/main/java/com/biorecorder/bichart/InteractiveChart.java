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
            chart.autoScaleSelectedTraceX();
            chart.autoScaleSelectedTraceY();
        } else {
            chart.autoScaleX();
            chart.autoScaleY();
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
        if(chart.isTraceSelected()) {
            chart.zoomSelectedTraceX(scaleFactor);

        } else if (startPoint != null){
            XAxisPosition xPosition = chart.getXAxisPosition(startPoint);
            chart.zoomX(xPosition, scaleFactor);
        } else {
            chart.zoomX(XAxisPosition.BOTTOM, scaleFactor);
            chart.zoomX(XAxisPosition.TOP, scaleFactor);
        }

        return true;
    }

    @Override
    public boolean onScrollX(@Nullable BPoint startPoint, int dx) {
        if(dx == 0) {
            return false;
        }
        if(chart.isTraceSelected()) {
            chart.translateSelectedTraceX(dx);
        } else if (startPoint != null){
            XAxisPosition xPosition = chart.getXAxisPosition(startPoint);
            chart.translateX(xPosition, dx);
        } else {
            chart.translateX(XAxisPosition.BOTTOM, dx);
            chart.translateX(XAxisPosition.TOP, dx);
        }
        return true;
    }

    @Override
    public boolean onScaleY(@Nullable BPoint startPoint, double scaleFactor) {
        if(scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if(chart.isTraceSelected()) {
            chart.zoomSelectedTraceY(scaleFactor);
            return true;
        } else if (startPoint != null) {
            int stack = chart.getStack(startPoint);
            YAxisPosition yPosition = chart.getYAxisPosition(stack, startPoint);
            chart.zoomY(stack, yPosition, scaleFactor);
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
            chart.translateSelectedTraceY(dy);
            return true;
        }
        if (startPoint != null) {
            int stack = chart.getStack(startPoint);
            YAxisPosition yPosition = chart.getYAxisPosition(stack, startPoint);
            chart.translateY(stack, yPosition, dy);
            return true;
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
