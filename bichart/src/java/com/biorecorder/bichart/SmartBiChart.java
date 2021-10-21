package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;

import java.util.ArrayList;
import java.util.List;

public class SmartBiChart extends BiChart {
    private DataProcessor dataProcessor;
    private boolean isConfigured = false;
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
    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter, boolean isXOpposite,  boolean isYOpposite) {
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
    public void setChartTraceData(int traceNumber, XYSeries data) {
        dataProcessor.setChartTraceData(traceNumber, data);
        chartTraceNeedUpdateDataFlags.set(traceNumber, true);
    }

    @Override
    public void setNavigatorTraceData(int traceNumber, XYSeries data) {
        dataProcessor.setNavigatorTraceData(traceNumber, data);
        navTraceNeedUpdateDataFlags.set(traceNumber, true);
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
        XYSeries data = dataProcessor.getProcessedChartData(traceNumber, chart.getXMin(chart.getTraceXPosition(traceNumber)), chart.getXMax(chart.getTraceXPosition(traceNumber)), xLength, getChartTraceMarkSize(traceNumber));
        chart.setTraceData(traceNumber, data);
        chartTraceNeedUpdateDataFlags.set(traceNumber, false);
    }

    private void setNavigatorTraceData(int traceNumber) {
        double xLength = getXLength();
        XYSeries data = dataProcessor.getProcessedNavigatorData(traceNumber, xLength, getNavigatorTraceMarkSize(traceNumber));
        navigator.setTraceData(traceNumber, data);
        navTraceNeedUpdateDataFlags.set(traceNumber, false);
    }


    private void configure() {
        autoScaleX();
        for (int i = 0; i < chartTraceNeedUpdateDataFlags.size(); i++) {
            if(chartTraceNeedUpdateDataFlags.get(i)) {
                setChartTraceData(i);
            }
        }
        for (int i = 0; i < navTraceNeedUpdateDataFlags.size(); i++) {
            if(navTraceNeedUpdateDataFlags.get(i)) {
                setNavigatorTraceData(i);
            }
        }
        autoScaleNavigatorY();
        autoScaleChartY();
        isConfigured = true;
    }

    @Override
    public void draw(BCanvas canvas) {
        revalidate(canvas.getRenderContext());
        if (!isConfigured) {
            configure();
        }
        for (int i = 0; i < chartTraceNeedUpdateDataFlags.size(); i++) {
           if(chartTraceNeedUpdateDataFlags.get(i)) {
               setChartTraceData(i);
           }
        }
        for (int i = 0; i < navTraceNeedUpdateDataFlags.size(); i++) {
            if(navTraceNeedUpdateDataFlags.get(i)) {
                setNavigatorTraceData(i);
            }
        }
        super.draw(canvas);
    }
}
