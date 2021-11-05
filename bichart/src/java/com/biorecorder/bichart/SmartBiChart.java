package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.scroll.Scroll;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;

import java.util.ArrayList;
import java.util.List;

public class SmartBiChart extends BiChart {
    private DataProcessor dataProcessor;
    private List<Boolean> chartTraceNeedUpdateDataFlags = new ArrayList<>();
    private List<Boolean> navTraceNeedUpdateDataFlags = new ArrayList<>();

    public SmartBiChart(ProcessingConfig processingConfig, BiChartConfig chartConfig, boolean isDateTime) {
        super(chartConfig, isDateTime);
        dataProcessor = new DataProcessor(processingConfig, isDateTime);
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            ScrollListener l = new ScrollListener() {
                @Override
                public void onScrollChanged(double viewportMin, double viewportMax) {
                    List<Integer> traceNumbers = chart.getTraces(xPosition);
                    for (int i = 0; i < traceNumbers.size(); i++) {
                        int traceNumber = traceNumbers.get(i);
                        chartTraceNeedUpdateDataFlags.set(traceNumber, true);
                    }
                }
            };
            axisToScrollListeners.get(xPosition).add(l);
        }
    }

    public SmartBiChart(boolean isDateTime) {
        this(new ProcessingConfig(), DarkTheme.getNavigableChartConfig(), isDateTime);
    }

    @Override
    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter, boolean isXOpposite, boolean isYOpposite) {
        super.addChartTrace(name, data, tracePainter, isXOpposite, isYOpposite);
        dataProcessor.addChartTraceData(data);
        chartTraceNeedUpdateDataFlags.add(true);
    }

    @Override
    public void addNavigatorTrace(String name, XYSeries data, TracePainter tracePainter, boolean isYOpposite) {
        super.addNavigatorTrace(name, data, tracePainter, isYOpposite);
        dataProcessor.addNavigatorTraceData(data);
        navTraceNeedUpdateDataFlags.add(true);
    }

    @Override
    public void removeChartTrace(int traceNumber) {
        super.removeChartTrace(traceNumber);
        chartTraceNeedUpdateDataFlags.remove(traceNumber);
        dataProcessor.removeChartTraceData(traceNumber);
    }

    @Override
    public void removeNavigatorTrace(int traceNumber) {
        super.removeNavigatorTrace(traceNumber);
        navTraceNeedUpdateDataFlags.remove(traceNumber);
        dataProcessor.removeNavigatorTraceData(traceNumber);
    }

    private void setChartTraceData(int traceNumber) {
        double xLength = getXLength();
        XAxisPosition xAxisPosition = chart.getTraceXPosition(traceNumber);
        XYSeries data = dataProcessor.getProcessedChartData(traceNumber, chart.getXMin(xAxisPosition), chart.getXMax(xAxisPosition), xLength, getChartTraceMarkSize(traceNumber));
        chart.setTraceData(traceNumber, data);
        chartTraceNeedUpdateDataFlags.set(traceNumber, false);
    }

    private void setNavigatorTraceData(int traceNumber) {
        double xLength = getXLength();
        XYSeries data = dataProcessor.getProcessedNavigatorData(traceNumber, xLength, getNavigatorTraceMarkSize(traceNumber));
        navigator.setTraceData(traceNumber, data);
        navTraceNeedUpdateDataFlags.set(traceNumber, false);
    }

    @Override
    public void autoScaleNavigatorX() {
        Range xMinMax = null;
        for (int i = 0; i < navigator.traceCount(); i++) {
            xMinMax = Range.join(xMinMax, dataProcessor.getNavigatorTraceDataRange(i));
        }
        for (int i = 0; i < chart.traceCount(); i++) {
            xMinMax = Range.join(xMinMax, dataProcessor.getChartTraceDataRange(i));
        }
        if (xMinMax != null) {
            navigator.setXMinMax(navDefaultXPosition, xMinMax.getMin(), xMinMax.getMax());
            for (XAxisPosition xPosition : axisToScrolls.keySet()) {
                Scroll scroll = axisToScrolls.get(xPosition);
                scroll.setMinMax(xMinMax.getMin(), xMinMax.getMax());
            }
        }
    }

    @Override
    protected void configure() {
        boolean scrollCreated = false;
        int viewportExtent = getXLength();
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            if(createScroll(xPosition, viewportExtent)) {
                List<Integer> traceNumbers = chart.getTraces(xPosition);
                for (int i = 0; i < traceNumbers.size(); i++) {
                    int traceNumber = traceNumbers.get(i);
                    chartTraceNeedUpdateDataFlags.set(traceNumber, true);
                }
                scrollCreated = true;
            }
        }
        boolean scrollAtTheEnd = true;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            Scroll scroll = axisToScrolls.get(xPosition);
            if(scroll != null && !scroll.isViewportAtTheEnd()) {
                scrollAtTheEnd = false;
            }
        }
        autoScaleNavigatorX();
        if(scrollAtTheEnd) {
            for (XAxisPosition xPosition : axisToScrolls.keySet()) {
                Scroll scroll = axisToScrolls.get(xPosition);
                if(scroll != null) {
                    scroll.setViewportAtTheEnd();
                }
            }
           /* for (int i = 0; i < chart.traceCount(); i++) {
                setChartTraceData(i);
            }
            for (int i = 0; i < navigator.traceCount(); i++) {
                setNavigatorTraceData(i);
            }*/
        }
        for (int i = 0; i < chartTraceNeedUpdateDataFlags.size(); i++) {
            if (chartTraceNeedUpdateDataFlags.get(i)) {
                setChartTraceData(i);
            }
        }
        for (int i = 0; i < navTraceNeedUpdateDataFlags.size(); i++) {
            if (navTraceNeedUpdateDataFlags.get(i)) {
                setNavigatorTraceData(i);
            }
        }
        if(scrollCreated) {
            autoScaleChartY();
        }
        autoScaleNavigatorY();
    }

    public void appendChartTraceData(int traceNumber, XYSeries data) {
        dataProcessor.appendChartTraceData(traceNumber, data);
        isDataChanged = true;
        //chartTraceNeedUpdateDataFlags.set(traceNumber, false);
    }

    public void appendNavigatorTraceData(int traceNumber, XYSeries data) {
        dataProcessor.appendNavigatorTraceData(traceNumber, data);
        isDataChanged = true;
        //navTraceNeedUpdateDataFlags.set(traceNumber, true);
    }

    public void dataAppended() {
        dataProcessor.dataAppended();
        isDataChanged = true;
    }
}
