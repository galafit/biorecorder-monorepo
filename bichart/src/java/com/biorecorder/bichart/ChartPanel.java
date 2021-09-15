package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;

import javax.swing.*;


public class ChartPanel extends JPanel  {
    final int scrollPointsPerRotation = 10;
    // во сколько раз растягивается или сжимается ось при автозуме
    private double defaultZoom = 2;
    private BPoint pressPoint;
    private int pastX;
    private int pastY;
    private boolean isXDirection;
    private boolean isYDirection;

    private BiChart chart;

    public ChartPanel(boolean isDateTime) {
        chart = new BiChart(DarkTheme.getNavigableChartConfig(), new LinearScale());
    }

    public void setChartTraceData(int traceNumber, XYSeries data) {
        chart.setChartTraceData(traceNumber, data);
    }

    public void setNavigatorTraceData(int traceNumber, XYSeries data) {
        chart.setNavigatorTraceData(traceNumber, data);
    }

    public void autoScaleX() {
        chart.autoScaleX();
    }

    public void autoScaleX(XAxisPosition xPosition) {
        chart.autoScaleX(xPosition);
    }

    public void setChartYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        chart.setChartYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleChartY() {
        chart.autoScaleChartY();
    }

    public void addChartStack() {
        chart.addChartStack();
    }

    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter) {
        chart.addChartTrace(name, data, tracePainter);
    }

    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter, boolean isXOpposite,  boolean isYOpposite) {
        chart.addChartTrace(name, data, tracePainter, isXOpposite, isYOpposite);
    }

    public Range getChartXRange() {
        return null; //chart.getChartXMinMax(XAxisPosition.TOP);
    }

    public void addNavigatorStack() {
        chart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYSeries data, TracePainter tracePainter) {
        chart.addNavigatorTrace(name, data, tracePainter);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        chart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        chart.autoScaleNavigatorY();
    }

}
