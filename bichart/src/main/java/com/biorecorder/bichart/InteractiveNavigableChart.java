package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.RenderContext;

/**
 * Created by galafit on 23/9/18.
 */
public class InteractiveNavigableChart implements InteractiveDrawable {
    private final NavigableChart navigableChart;
    private BPoint lastStartPoint;
    private boolean isScrollMoving;

    public InteractiveNavigableChart(NavigableChart navigableChart) {
        this.navigableChart = navigableChart;
    }

    @Override
    public void onResize(int width, int height) {
        navigableChart.setSize(width, height);
    }

    @Override
    public boolean onTap(int x, int y) {
        if(navigableChart.selectTrace(x, y)) {
            return true;
        } else {
            if(navigableChart.navigatorContain(x, y))  {
                return navigableChart.setScrollsPosition(x);
            }
            return false;
        }
    }

    @Override
    public boolean onDoubleTap(int x, int y) {
        // AUTO SCALE both chart and navigator
        if(navigableChart.isChartTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            navigableChart.autoScaleScrollExtent(navigableChart.getChartSelectedTraceX());
            navigableChart.autoScaleChartY(navigableChart.getChartSelectedTraceStack(), navigableChart.getChartSelectedTraceY());
        } else {
            // if no selected trace in chart we scale all x and y axis
            navigableChart.autoScaleScrollExtent();
            navigableChart.autoScaleChartY();
        }
        // do the same with navigator...
        if(navigableChart.isNavigatorTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            navigableChart.autoScaleNavigatorY(navigableChart.getNavigatorSelectedTraceStack(), navigableChart.getNavigatorSelectedTraceY());
        } else {
            // if no selected trace in navigator we scale all  y axis
            navigableChart.autoScaleNavigatorY();
        }
        return true;
    }

    @Override
    public boolean onTapUp(int x, int y) {
        isScrollMoving = false;
        return navigableChart.hoverOff();
    }

    @Override
    public boolean onLongPress(int x, int y) {
        return navigableChart.hoverOn(x, y);
    }

    @Override
    public boolean onScaleX(BPoint startPoint, double scaleFactor) {
        if (scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if(navigableChart.isChartTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            return navigableChart.zoomScrollExtent(navigableChart.getChartSelectedTraceX(), scaleFactor);
        } else {
            return navigableChart.zoomScrollExtent(scaleFactor);
        }
    }


    @Override
    public boolean onScrollX(BPoint startPoint, int dx) {
        if(dx == 0) {
            return false;
        }
        if(startPoint != null && !startPoint.equals(lastStartPoint)){
            lastStartPoint = startPoint;
            isScrollMoving = navigableChart.scrollContain(startPoint.getX(), startPoint.getY());
        }

        if(isScrollMoving) {
            return navigableChart.translateScrolls(-dx);
        }

        if(startPoint == null || navigableChart.chartContain(startPoint.getX(), startPoint.getY())) {
            return navigableChart.translateScrollsViewport(dx);
        }

        return false;
    }

    @Override
    public boolean onScaleY(BPoint startPoint, double scaleFactor) {
        if (startPoint == null || scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if(navigableChart.chartContain(startPoint.getX(), startPoint.getY())) {
            if(navigableChart.isChartTraceSelected()) {
                navigableChart.zoomChartY(navigableChart.getChartSelectedTraceStack(), navigableChart.getChartSelectedTraceY(), scaleFactor);
                return true;
            } else {
                int stack = navigableChart.getChartStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getChartYAxis(stack, startPoint);
                    if(yPosition != null) {
                        navigableChart.zoomChartY(stack, yPosition, scaleFactor);
                        return true;
                    }
                }
                return false;
            }
        } else {
            if(navigableChart.isNavigatorTraceSelected()) {
                navigableChart.zoomNavigatorY(navigableChart.getNavigatorSelectedTraceStack(), navigableChart.getNavigatorSelectedTraceY(), scaleFactor);
                return true;
            } else {
                int stack = navigableChart.getNavigatorStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getNavigatorYAxis(stack, startPoint);
                    if(yPosition != null) {
                        return navigableChart.zoomNavigatorY(stack, yPosition, scaleFactor);
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
        if(navigableChart.chartContain(startPoint.getX(), startPoint.getY())) {
            if(navigableChart.isChartTraceSelected()) {
                navigableChart.translateChartY(navigableChart.getChartSelectedTraceStack(), navigableChart.getChartSelectedTraceY(), dy);
                return true;
            } else {
                int stack = navigableChart.getChartStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getChartYAxis(stack, startPoint);
                    if(yPosition != null) {
                        navigableChart.translateChartY(stack, yPosition, dy);
                        return true;
                    }
                }
                return false;
            }
        } else {
            if(navigableChart.isNavigatorTraceSelected()) {
                navigableChart.translateNavigatorY(navigableChart.getNavigatorSelectedTraceStack(), navigableChart.getNavigatorSelectedTraceY(), dy);
                return true;
            } else {
                int stack = navigableChart.getNavigatorStack(startPoint);
                if(stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getNavigatorYAxis(stack, startPoint);
                    if(yPosition != null) {
                        navigableChart.translateNavigatorY(stack, yPosition, dy);
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override
    public void draw(BCanvas canvas) {
        navigableChart.draw(canvas);
    }

    @Override
    public boolean update(RenderContext renderContext) {
        return true;
    }
}
