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
            if(chart.navigatorContain(x, y))  {
                return chart.setScrollsPosition(x);
            }
            return false;
        }
    }

    @Override
    public boolean onDoubleTap(int x, int y) {
        // AUTO SCALE both chart and navigator
        if(chart.isChartTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            chart.autoScaleScrollExtent(chart.getChartSelectedTraceX());
            chart.autoScaleChartY(chart.getChartSelectedTraceStack(), chart.getChartSelectedTraceY());
        } else {
            // if no selected trace in chart we scale all x and y axis
            chart.autoScaleScrollExtent();
            chart.autoScaleChartY();
        }
        // do the same with navigator...
        if(chart.isNavigatorTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            chart.autoScaleNavigatorY(chart.getNavigatorSelectedTraceStack(), chart.getNavigatorSelectedTraceY());
        } else {
            // if no selected trace in navigator we scale all  y axis
            chart.autoScaleNavigatorY();
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
            return chart.zoomScrollExtent(scaleFactor);
        }
    }


    @Override
    public boolean onScrollX(BPoint startPoint, int dx) {
        if(dx == 0) {
            return false;
        }
        if(startPoint != null && !startPoint.equals(lastStartPoint)){
            lastStartPoint = startPoint;
            isScrollMoving = chart.scrollContain(startPoint.getX(), startPoint.getY());
        }

        if(isScrollMoving) {
            return chart.translateScrolls(-dx);
        }

        if(startPoint == null || chart.chartContain(startPoint.getX(), startPoint.getY())) {
            return chart.translateScrollsViewport(dx);
        }

        return false;
    }

    @Override
    public boolean onScaleY(BPoint startPoint, double scaleFactor) {
        if (startPoint == null || scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if(chart.chartContain(startPoint.getX(), startPoint.getY())) {
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
                        return chart.zoomNavigatorY(stack, yPosition, scaleFactor);
                    }
                }
                return false;
            }
        }
    }


    @Override
    public boolean onScrollY(BPoint startPoint, int dy) {
        if(dy == 0 || startPoint == null) {
            return false;
        }
        if(chart.chartContain(startPoint.getX(), startPoint.getY())) {
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
