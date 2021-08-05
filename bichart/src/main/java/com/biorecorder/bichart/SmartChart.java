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
    private List<XYData> navigatorRowData = new ArrayList<>();
    private List<XYData> chartRowData = new ArrayList<>();
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

    // suppose that data is ordered
    public Range dataMinMax(XYData data) {
        if(data != null) {
            return new Range(data.xValue(0), data.xValue(data.rowCount() - 1));
        }
        return null;
    }

    private Range dataBestRange(XYData data, int markSize, Scale xScale, int xLength, double min) {
        int dataSize = data.rowCount();
        Range dataRange = dataMinMax(data);
        if(dataSize > 1 && dataRange != null && dataRange.length() > 0) {
            xScale.setStartEnd(1, dataSize * markSize);
            xScale.setMinMax(dataRange.getMin(), dataRange.getMax());
            int positionOfMin = (int)xScale.scale(min);
            int positionOfMax = positionOfMin + xLength;
            double max = xScale.invert(positionOfMax);
            return new Range(min, max);
        }
        return null;
    }

    private void autoScaleNavigatorX() {
        Scale xScale = navigableChart.getNavigatorXScale();
        int xLength = (int)Math.round(xScale.getLength());
        Range xMinMax = null;
        for (int i = 0; i < navigableChart.navigatorTraceCount(); i++) {
            XYData data = navigatorRowData.get(i);
            xMinMax = Range.join(xMinMax, dataMinMax(data));
        }
        if(xMinMax != null) {
            for (int i = 0; i < navigableChart.navigatorTraceCount(); i++) {
                Range dataBestRange = dataBestRange(navigatorRowData.get(i), navigableChart.getNavigatorTraceMarkSize(i), xScale, xLength, xMinMax.getMin());
                if(dataBestRange != null && dataBestRange.length() > 0) {
                    if(dataBestRange.getMax() > xMinMax.getMax()) {
                        xMinMax = new Range(xMinMax.getMin(), dataBestRange.getMax());
                    }
                }
            }
            navigableChart.setNavigatorXMinMax(xMinMax.getMin(), xMinMax.getMax());
        }
    }

    private void autoScaleChartX() {
        Map<XAxisPosition, List<Integer>> xPositionToTracesNumbers = new HashMap<>();
        for (int i = 0; i < navigableChart.chartTraceCount(); i++) {
            XAxisPosition xPosition = navigableChart.getChartTraceXAxisPosition(i);
            List<Integer> traceNumbers = xPositionToTracesNumbers.get(xPosition);
            if(traceNumbers == null) {
                traceNumbers = new ArrayList<>();
                xPositionToTracesNumbers.put(xPosition, traceNumbers);
            }
            traceNumbers.add(i);
        }
        for (XAxisPosition xPosition : xPositionToTracesNumbers.keySet()) {
            List<Integer> traceNumbers = xPositionToTracesNumbers.get(xPosition);
            Scale xScale = navigableChart.getChartXScale(xPosition);
            int xLength = (int)Math.round(xScale.getLength());
            Range xMinMax = null;
            for (int i = 0; i < traceNumbers.size(); i++) {
                Range dataBestRange = dataBestRange(chartRowData.get(traceNumbers.get(i)), navigableChart.getChartTraceMarkSize(traceNumbers.get(i)), xScale, xLength, xMinMax.getMin());
                if(dataBestRange != null && dataBestRange.length() > 0) {
                    if(xMinMax == null || (dataBestRange != null && dataBestRange.getMax() < xMinMax.getMax())) {
                        xMinMax = dataBestRange;
                    }
                }
            }
            if(xMinMax != null) {
                navigableChart.setScrollRange(xPosition, xMinMax);
            }
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
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigableChart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        navigableChart.autoScaleNavigatorY();
    }
}
