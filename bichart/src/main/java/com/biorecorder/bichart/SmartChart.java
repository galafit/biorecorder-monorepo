package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scales.TimeScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;

import java.util.ArrayList;


public class SmartChart {
    private DataProcessor dataProcessor;
    private NavigableChart1 navigableChart;
    private boolean isConfigured = false;

    public SmartChart(ProcessingConfig processingConfig, NavigableChartConfig chartConfig, Scale xScale, boolean isDateTime) {
        dataProcessor = new DataProcessor(processingConfig, isDateTime);
        this.navigableChart = new NavigableChart1(chartConfig, xScale);
    }

    public static SmartChart createChart() {
        return new SmartChart(new ProcessingConfig(), DarkTheme.getNavigableChartConfig(), new LinearScale(), false);
    }

    public static SmartChart createTimeChart() {
        return new SmartChart(new ProcessingConfig(), DarkTheme.getNavigableChartConfig(), new TimeScale(), true);
    }

    private void inti() {
        int xLength;

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
        navigableChart.addChartTrace(name, data.getEmptyCopy(), tracePainter);
    }

    public void addNavigatorStack() {
        navigableChart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, TracePainter tracePainter) {
        navigableChart.addNavigatorTrace(name, data.getEmptyCopy(), tracePainter);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigableChart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        navigableChart.autoScaleNavigatorY();
    }

}
