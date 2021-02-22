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
            chart.autoScaleSelectedTraceXScrollExtent();
            chart.autoScaleChartSelectedTraceY();
        } else {
            // if no selected trace in chart we scale all x and y axis
            chart.autoScaleScrollExtent();
            chart.autoScaleChartY();
        }
        // do the same with navigator...
        if(chart.isNavigatorTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            chart.autoScaleNavigatorSelectedTraceY();
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
            boolean isChanged = false;
            for (XAxisPosition xPosition : XAxisPosition.values()) {
                if(chart.hasScroll(xPosition)) {
                    isChanged = chart.zoomScrollExtent(xPosition, scaleFactor) || isChanged;
                }
            }
            return isChanged;
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

        if(startPoint == null || chart.isChartContains(startPoint.getX(), startPoint.getY())) {
            double scrollTranslation = 0;
            if(chart.isChartTraceSelected()) {
                XAxisPosition xPosition = chart.getChartSelectedTraceX();
                if(chart.hasScroll(xPosition)) {
                    scrollTranslation = chart.getScrollWidth(xPosition) / chart.getWidth();
                }
            } else {
                for (XAxisPosition xPosition : XAxisPosition.values()) {
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
    public boolean onScaleY(BPoint startPoint, double scaleFactor) {
        if (startPoint == null || scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if(chart.isChartContains(startPoint.getX(), startPoint.getY())) {
            if(chart.isChartTraceSelected()) {
                chart.zoomChartSelectedTraceY(scaleFactor);
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
                chart.zoomNavigatorSelectedTraceY(scaleFactor);
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
    public boolean onScrollY(BPoint startPoint, int dy) {
        if(dy == 0 || startPoint == null) {
            return false;
        }
        if(chart.isChartContains(startPoint.getX(), startPoint.getY())) {
            if(chart.isChartTraceSelected()) {
                chart.translateChartSelectedTraceY(dy);
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
                chart.translateNavigatorSelectedTraceY(dy);
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
