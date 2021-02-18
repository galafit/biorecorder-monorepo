package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.RenderContext;

/**
 * Created by galafit on 23/9/18.
 */
public class InteractiveNavigableChart implements InteractiveDrawable {
    private final NavigableChart chart;
    private BPoint lastStartPoint;
    private boolean isScrollMoving;

    public InteractiveNavigableChart(NavigableChart chart) {
        this.chart = chart;
    }

    @Override
    public void onResize(int width, int height) {
        chart.setSize(width, height);
    }

    @Override
    public boolean onTap(int x, int y) {
        if(chart.selectTrace(x, y)) {
            return true;
        } else {
            if(chart.isNavigatorContains(new BPoint(x, y)))  {
                return chart.setScrollsPosition(x, y);
            }
            return false;
        }
    }

    @Override
    public boolean onDoubleTap(int x, int y) {
        // AUTO SCALE both chart and navigator
        if(chart.isChartTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            chart.autoScaleChartY(chart.getChartSelectedTraceStack(), chart.getChartSelectedTraceY());
        } else {
            // if no selected trace in chart we scale all x and y axis
            XAxisPosition[] xAxisPositions = chart.getChartXAxes();
            for (int i = 0; i < xAxisPositions.length; i++) {
                XAxisPosition xPosition = xAxisPositions[i];
                if(chart.hasScroll(xPosition)) {
                    chart.autoScaleScrollExtent(xPosition);
                }
            }
            for (int stack = 0; stack < chart.chartStackCount(); stack++) {
                YAxisPosition[] yAxisPositions = chart.getChartYAxes(stack);
                for (int i = 0; i < yAxisPositions.length; i++) {
                    chart.autoScaleChartY(stack, yAxisPositions[i]);
                }
            }
        }

        // do the same with navigator...
        if(chart.isNavigatorTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            chart.autoScaleNavigatorY(chart.getNavigatorSelectedTraceStack(), chart.getNavigatorSelectedTraceY());
        } else {
            // if no selected trace in navigator we scale all  y axis

            for (int stack = 0; stack < chart.navigatorStackCount(); stack++) {
                YAxisPosition[] yAxisPositions = chart.getNavigatorYAxes(stack);
                for (int i = 0; i < yAxisPositions.length; i++) {
                    chart.autoScaleChartY(stack, yAxisPositions[i]);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onTapUp(int x, int y) {
        isScrollMoving = false;
        return chart.hoverOff();
    }

    @Override
    public boolean onLongPress(int x, int y) {
        return chart.hoverOn(x, y);
    }

    @Override
    public boolean onScaleX(BPoint startPoint, double scaleFactor) {
        if (scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if(chart.isChartTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            return chart.zoomScrollExtent(chart.getChartSelectedTraceX(), scaleFactor);
        } else {
            boolean isChanged = false;
            XAxisPosition[] xAxisPositions = chart.getChartXAxes();
            for (int i = 0; i < xAxisPositions.length; i++) {
                XAxisPosition xPosition = xAxisPositions[i];
                if(chart.hasScroll(xPosition)) {
                    isChanged = chart.zoomScrollExtent(xPosition, scaleFactor) || isChanged;
                }
            }
            return isChanged;
        }
    }

    @Override
    public boolean onScaleY(BPoint startPoint, double scaleFactor) {
        if (startPoint == null || scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if(chart.isChartContains(startPoint)) {
            if(chart.isChartTraceSelected()) {
                chart.zoomChartY(chart.getChartSelectedTraceStack(), chart.getChartSelectedTraceY(), scaleFactor);
                return true;
            } else {
                int stack = chart.getChartStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = chart.getChartYAxis(stack, startPoint);
                    if(yPosition != null) {
                        chart.zoomChartY(stack, yPosition, scaleFactor);
                        return true;
                    }
                }
                return false;
            }
        } else {
            if(chart.isNavigatorTraceSelected()) {
                chart.zoomNavigatorY(chart.getNavigatorSelectedTraceStack(), chart.getNavigatorSelectedTraceY(), scaleFactor);
                return true;
            } else {
                int stack = chart.getNavigatorStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = chart.getNavigatorYAxis(stack, startPoint);
                    if(yPosition != null) {
                        chart.zoomNavigatorY(stack, yPosition, scaleFactor);
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override
    public boolean onScrollX(BPoint startPoint, int dx) {
        if(dx == 0) {
            return false;
        }

        if(startPoint != null && !startPoint.equals(lastStartPoint)){
            lastStartPoint = startPoint;
            isScrollMoving = chart.isScrollContain(startPoint.getX(), startPoint.getY());
        }

        if(isScrollMoving) {
            chart.translateScrolls(-dx);
            return true;
        }

        if(startPoint == null || chart.isChartContains(startPoint)) {
            double scrollTranslation = 0;
            if(chart.isChartTraceSelected()) {
                XAxisPosition xPosition = chart.getChartSelectedTraceX();
                if(chart.hasScroll(xPosition)) {
                    scrollTranslation = chart.getScrollWidth(xPosition) / chart.getWidth();
                }
            } else {
                XAxisPosition[] xAxisPositions = chart.getChartXAxes();
                for (int i = 0; i < xAxisPositions.length; i++) {
                    XAxisPosition xPosition = xAxisPositions[i];
                    if(chart.hasScroll(xPosition)) {
                        double translation = chart.getScrollWidth(xPosition) / chart.getWidth();
                        scrollTranslation = Math.max(scrollTranslation, translation);
                    }
                }
            }
            chart.translateScrolls(dx * scrollTranslation);
            return true;
        }

        return false;
    }

    @Override
    public boolean onScrollY(BPoint startPoint, int dy) {
        if(dy == 0 || startPoint == null) {
            return false;
        }

        if(chart.isChartContains(startPoint)) {
            if(chart.isChartTraceSelected()) {
                chart.translateChartY(chart.getChartSelectedTraceStack(), chart.getChartSelectedTraceY(), dy);
                return true;
            } else {
                int stack = chart.getChartStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = chart.getChartYAxis(stack, startPoint);
                    if(yPosition != null) {
                        chart.translateChartY(stack, yPosition, dy);
                        return true;
                    }
                }
                return false;
            }
        } else {
            if(chart.isNavigatorTraceSelected()) {
                chart.translateNavigatorY(chart.getNavigatorSelectedTraceStack(), chart.getNavigatorSelectedTraceY(), dy);
                return true;
            } else {
                int stack = chart.getNavigatorStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = chart.getNavigatorYAxis(stack, startPoint);
                    if(yPosition != null) {
                        chart.translateNavigatorY(stack, yPosition, dy);
                        return true;
                    }
                }
                return false;
            }
        }
    }


    @Override
    public void draw(BCanvas canvas) {
        chart.draw(canvas);

    }

    @Override
    public boolean update(RenderContext renderContext) {
        return true;
    }
}
