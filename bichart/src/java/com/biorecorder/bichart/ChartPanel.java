package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.chart.SmartChart;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.traces.TracePainter;


public class ChartPanel extends InteractivePanel {
    private SmartChart smartChart;

    public ChartPanel(boolean isDateTime) {
        super(new SmartChart(isDateTime));
        smartChart = (SmartChart) chart;
    }

    public void setChartTraceData(int traceNumber, XYData data) {
        smartChart.setChartTraceData(traceNumber, data);
    }

    public void setNavigatorTraceData(int traceNumber, XYData data) {
        smartChart.setNavigatorTraceData(traceNumber, data);
    }

    public void appendNavigatorTraceData(int traceNumber, XYData dataToAppend) {
        smartChart.appendNavigatorTraceData(traceNumber, dataToAppend);
    }

    public void autoScaleX() {
        smartChart.autoScaleX();
    }

    public void autoScaleX(XAxisPosition xPosition) {
        smartChart.autoScaleX(xPosition);
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

    public Range getChartXRange() {
        return smartChart.getChartXMinMax(XAxisPosition.TOP);
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
        smartChart.onResize(width, height);
    }
}
