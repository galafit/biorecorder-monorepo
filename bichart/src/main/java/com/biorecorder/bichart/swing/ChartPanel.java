package com.biorecorder.bichart.swing;

import com.biorecorder.bichart.SmartChart;
import com.biorecorder.bichart.XYData;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.traces.TracePainter;


public class ChartPanel extends InteractivePanel {
    private SmartChart smartChart;
    final int scrollPointsPerRotation = 10;
    // во сколько раз растягивается или сжимается ось при автозуме
    private double defaultZoom = 2;
    private BPoint pressPoint;
    private int pastX;
    private int pastY;
    private boolean isXDirection;
    private boolean isYDirection;

    public ChartPanel(boolean isDateTime) {
        super(new SmartChart(isDateTime));
        smartChart = (SmartChart) chart;
    }

    public void setChartTraceData(int traceNumber, XYData data) {
        smartChart.setChartTraceData(traceNumber, data);
    }

    public void appendNavigatorTraceData(int traceNumber, XYData dataToAppend) {
        smartChart.appendNavigatorTraceData(traceNumber, dataToAppend);
    }

    public void autoScaleNavigatorX() {
        smartChart.autoScaleNavigatorX();
    }

    public void autoScaleChartX() {
        smartChart.autoScaleChartX();
    }

    public void setChartYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        smartChart.setChartYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleChartY() {
        smartChart.autoScaleChartY();
    }

    public void addChartStack() {
        smartChart.addChartStack();
    }

    public void addChartTrace(String name, XYData data, TracePainter tracePainter) {
        smartChart.addChartTrace(name, data, tracePainter);
    }

    public void addNavigatorStack() {
        smartChart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, TracePainter tracePainter) {
        smartChart.addNavigatorTrace(name, data, tracePainter);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        smartChart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        smartChart.autoScaleNavigatorY();
    }

    public void setSize(int width, int height) {
        smartChart.setSize(width, height);
    }
}
