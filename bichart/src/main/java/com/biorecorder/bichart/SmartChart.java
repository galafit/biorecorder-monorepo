package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.graphics.RenderContext;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;

import java.util.List;

public class SmartChart implements InteractiveDrawable {
    private DataProcessor dataProcessor;
    private NavigableChart navigableChart;
    private boolean isConfigured = false;

    private BPoint lastStartPoint;
    private boolean isScrollMoving;

    public SmartChart(ProcessingConfig processingConfig, NavigableChartConfig chartConfig, Scale xScale, boolean isDateTime) {
        dataProcessor = new DataProcessor(processingConfig, isDateTime);
        navigableChart = new NavigableChart(chartConfig, xScale);
    }

    public SmartChart(boolean isDateTime) {
        this(new ProcessingConfig(), DarkTheme.getNavigableChartConfig(), new LinearScale(), isDateTime);
    }

    private void setChartTraceData(int traceNumber) {
        Range minMax = navigableChart.getChartXMinMax(navigableChart.getChartTraceXAxisPosition(traceNumber));
        XYData data = dataProcessor.getProcessedChartData(traceNumber, minMax, getXLength(), navigableChart.getChartTraceMarkSize(traceNumber));
        navigableChart.setChartTraceData(traceNumber, data);
    }

    private void setNavigatorTraceData(int traceNumber) {
        XYData data = dataProcessor.getProcessedNavigatorData(traceNumber, getXLength(), navigableChart.getNavigatorTraceMarkSize(traceNumber));
        navigableChart.setNavigatorTraceData(traceNumber, data);
    }


    // suppose that data is ordered
    private Range dataMinMax(XYData data) {
        if (data != null) {
            return new Range(data.xValue(0), data.xValue(data.rowCount() - 1));
        }
        return null;
    }

    public void autoScaleNavigatorX() {
        navigableChart.autoScaleNavigatorX();
    }

    public void autoScaleChartX() {
        navigableChart.autoScaleChartX();
    }

    private void updateNavigatorRange(XYData data) {
        Range dataMinMax = dataMinMax(data);
        Range navigatorRange = navigableChart.getNavigatorXMinMax();
        navigatorRange = Range.join(navigatorRange, dataMinMax);
        navigableChart.setNavigatorXMinMax(navigatorRange.getMin(), navigatorRange.getMax());
    }

    public void setChartTraceData(int traceNumber, XYData data) {
        dataProcessor.setChartTraceData(traceNumber, data);
        if (isConfigured) {
            setChartTraceData(traceNumber);
            updateNavigatorRange(data);
        }
    }

    public void appendNavigatorTraceData(int traceNumber, XYData dataToAppend) {
        dataProcessor.appendNavigatorTraceData(traceNumber, dataToAppend);
        if (isConfigured) {
            setNavigatorTraceData(traceNumber);
            updateNavigatorRange(dataToAppend);
        }
    }

    public void setChartYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigableChart.setChartYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleChartY() {
        navigableChart.autoScaleChartY();
    }

    public void addChartStack() {
        navigableChart.addChartStack();
    }

    public void addChartTrace(String name, XYData data, TracePainter tracePainter) {
        navigableChart.addChartTrace(name, data, tracePainter);
        dataProcessor.addChartTraceData(data);
    }

    public void addNavigatorStack() {
        navigableChart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, TracePainter tracePainter) {
        navigableChart.addNavigatorTrace(name, data, tracePainter);
        dataProcessor.addNavigatorTraceData(data);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigableChart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        navigableChart.autoScaleNavigatorY();
    }

    private int getXLength() {
        return (int) navigableChart.getNavigatorXScale().getLength();
    }

    private void configure() {
        for (int i = 0; i < navigableChart.chartTraceCount(); i++) {
            setChartTraceData(i);
        }
        for (int i = 0; i < navigableChart.navigatorTraceCount(); i++) {
            setNavigatorTraceData(i);
        }
        int xLength = getXLength();
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            ScrollListener l = new ScrollListener() {
                @Override
                public void onScrollChanged(double viewportMin, double viewportMax) {
                    List<Integer> traceNumbers = navigableChart.getChartTraces(xPosition);
                    for (int i = 0; i < traceNumbers.size(); i++) {
                        int traceNumber = traceNumbers.get(i);
                        XYData traceData = dataProcessor.getProcessedChartData(traceNumber, new Range(viewportMin, viewportMax), xLength, navigableChart.getChartTraceMarkSize(traceNumber));
                        navigableChart.setChartTraceData(traceNumber, traceData);
                    }
                }
            };
            navigableChart.addScrollListener(xPosition, l);
        }
        navigableChart.autoScaleNavigatorX();
        navigableChart.autoScaleChartX();
        navigableChart.autoScaleChartY();
        navigableChart.autoScaleNavigatorY();
        isConfigured = true;
    }

    @Override
    public void draw(BCanvas canvas) {
        if (!isConfigured) {
            navigableChart.revalidate(canvas.getRenderContext());
            configure();
        }
        navigableChart.draw(canvas);
    }

    @Override
    public void onResize(int width, int height) {
        navigableChart.setSize(width, height);
        if (isConfigured) {
            for (int i = 0; i < navigableChart.navigatorTraceCount(); i++) {
                setNavigatorTraceData(i);
            }
        }
    }

    @Override
    public boolean onTap(int x, int y) {
        if (navigableChart.selectTrace(x, y)) {
            return true;
        } else {
            if (navigableChart.navigatorContain(x, y)) {
                return navigableChart.setScrollsPosition(x);
            }
            return false;
        }
    }

    @Override
    public boolean onDoubleTap(int x, int y) {
        // AUTO SCALE both chart and navigator
        if (navigableChart.isChartTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            navigableChart.autoScaleChartX(navigableChart.getChartSelectedTraceX());
            navigableChart.autoScaleChartY(navigableChart.getChartSelectedTraceStack(), navigableChart.getChartSelectedTraceY());
        } else {
            // if no selected trace in chart we scale all x and y axis
            navigableChart.autoScaleChartX();
            navigableChart.autoScaleChartY();
        }
        // do the same with navigator...
        if (navigableChart.isNavigatorTraceSelected()) {
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
        if (navigableChart.isChartTraceSelected()) {
            // if some trace is selected we auto scale only axis belonging to that trace
            return navigableChart.zoomScrollExtent(navigableChart.getChartSelectedTraceX(), scaleFactor);
        } else {
            return navigableChart.zoomScrollExtent(scaleFactor);
        }
    }


    @Override
    public boolean onScrollX(BPoint startPoint, int dx) {
        if (dx == 0) {
            return false;
        }
        if (startPoint != null && !startPoint.equals(lastStartPoint)) {
            lastStartPoint = startPoint;
            isScrollMoving = navigableChart.scrollContain(startPoint.getX(), startPoint.getY());
        }

        if (isScrollMoving) {
            return navigableChart.translateScrolls(-dx);
        }

        if (startPoint == null || navigableChart.chartContain(startPoint.getX(), startPoint.getY())) {
            return navigableChart.translateScrollsViewport(dx);
        }

        return false;
    }

    @Override
    public boolean onScaleY(BPoint startPoint, double scaleFactor) {
        if (startPoint == null || scaleFactor == 0 || scaleFactor == 1) {
            return false;
        }
        if (navigableChart.chartContain(startPoint.getX(), startPoint.getY())) {
            if (navigableChart.isChartTraceSelected()) {
                navigableChart.zoomChartY(navigableChart.getChartSelectedTraceStack(), navigableChart.getChartSelectedTraceY(), scaleFactor);
                return true;
            } else {
                int stack = navigableChart.getChartStack(startPoint);
                if (stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getChartYAxis(stack, startPoint);
                    if (yPosition != null) {
                        navigableChart.zoomChartY(stack, yPosition, scaleFactor);
                        return true;
                    }
                }
                return false;
            }
        } else {
            if (navigableChart.isNavigatorTraceSelected()) {
                navigableChart.zoomNavigatorY(navigableChart.getNavigatorSelectedTraceStack(), navigableChart.getNavigatorSelectedTraceY(), scaleFactor);
                return true;
            } else {
                int stack = navigableChart.getNavigatorStack(startPoint);
                if (stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getNavigatorYAxis(stack, startPoint);
                    if (yPosition != null) {
                        return navigableChart.zoomNavigatorY(stack, yPosition, scaleFactor);
                    }
                }
                return false;
            }
        }
    }


    @Override
    public boolean onScrollY(BPoint startPoint, int dy) {
        if (dy == 0 || startPoint == null) {
            return false;
        }
        if (navigableChart.chartContain(startPoint.getX(), startPoint.getY())) {
            if (navigableChart.isChartTraceSelected()) {
                navigableChart.translateChartY(navigableChart.getChartSelectedTraceStack(), navigableChart.getChartSelectedTraceY(), dy);
                return true;
            } else {
                int stack = navigableChart.getChartStack(startPoint);
                if (stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getChartYAxis(stack, startPoint);
                    if (yPosition != null) {
                        navigableChart.translateChartY(stack, yPosition, dy);
                        return true;
                    }
                }
                return false;
            }
        } else {
            if (navigableChart.isNavigatorTraceSelected()) {
                navigableChart.translateNavigatorY(navigableChart.getNavigatorSelectedTraceStack(), navigableChart.getNavigatorSelectedTraceY(), dy);
                return true;
            } else {
                int stack = navigableChart.getNavigatorStack(startPoint);
                if (stack >= 0) {
                    YAxisPosition yPosition = navigableChart.getNavigatorYAxis(stack, startPoint);
                    if (yPosition != null) {
                        navigableChart.translateNavigatorY(stack, yPosition, dy);
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override
    public boolean update(RenderContext renderContext) {
        return true;
    }
}
