package com.biorecorder.bichart;
import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scales.TimeScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartChart {
    private DataProcessor dataProcessor;
    private NavigableChart1 navigableChart;
    private boolean isValid = false;

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

    private void autoScaleNavigatorX() {
        List<Integer> markSizes = new ArrayList<>();
        for (int i = 0; i < navigableChart.navigatorTraceCount(); i++) {
            markSizes.add(navigableChart.getNavigatorTraceMarkSize(i));
        }
        Range xMinMax = dataProcessor.getNavigatorTracesBestRange(navigableChart.getNavigatorXScale(), markSizes);
        if(xMinMax != null) {
            navigableChart.setNavigatorXMinMax(xMinMax.getMin(), xMinMax.getMax());
        }
    }

    private void autoScaleChartX() {
        Map<XAxisPosition, List<Integer>> xPositionToTracesNumbers = new HashMap<>();
        Map<Integer,Integer> traceNumberToMarkSizes = new HashMap<>();
        for (int i = 0; i < navigableChart.chartTraceCount(); i++) {
            XAxisPosition xPosition = navigableChart.getChartTraceXAxisPosition(i);
            List<Integer> traceNumbers = xPositionToTracesNumbers.get(xPosition);
            if(traceNumbers == null) {
                traceNumbers = new ArrayList<>();
                xPositionToTracesNumbers.put(xPosition, traceNumbers);
            }
            traceNumbers.add(i);
            traceNumberToMarkSizes.put(i, navigableChart.getChartTraceMarkSize(i));

        }
        for (XAxisPosition xPosition : xPositionToTracesNumbers.keySet()) {
            List<Integer> traceNumbers = xPositionToTracesNumbers.get(xPosition);
            Range bestRange = dataProcessor.getChartTracesBestRange(navigableChart.getChartXScale(xPosition), traceNumbers, traceNumberToMarkSizes);
            navigableChart.setScrollRange(xPosition, bestRange);
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
        navigableChart.addChartTrace(name, data.getEmptyCopy(), tracePainter);
        dataProcessor.addChartTrace(data);
    }

    public void addNavigatorStack() {
        navigableChart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, TracePainter tracePainter) {
        navigableChart.addNavigatorTrace(name, data.getEmptyCopy(), tracePainter);
        dataProcessor.addNavigatorTrace(data);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigableChart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        navigableChart.autoScaleNavigatorY();
    }
}
