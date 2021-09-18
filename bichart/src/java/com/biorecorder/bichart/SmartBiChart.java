package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.configs.BiChartConfig;
import com.biorecorder.bichart.configs.ProcessingConfig;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.TimeScale;
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
        super(chartConfig, new LinearScale());
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

    private void updateNavigatorRange(XYSeries data) {
        Range dataMinMax = dataMinMax(data);
        Range navigatorRange = navigator.getXMinMax(navDefaultXPosition);
        navigatorRange = Range.join(navigatorRange, dataMinMax);
        setNavigatorXMinMax(navigatorRange.getMin(), navigatorRange.getMax());
    }

    private void setChartTraceData(int traceNumber) {
        Range minMax = chart.getXMinMax(chart.getTraceXPosition(traceNumber));
        double xLength = navigator.getXScale(navDefaultXPosition).getLength();
        XYSeries data = dataProcessor.getProcessedChartData(traceNumber, minMax, xLength, getChartTraceMarkSize(traceNumber));
        chart.setTraceData(traceNumber, data);
    }

    private void setNavigatorTraceData(int traceNumber) {
        double xLength = navigator.getXScale(navDefaultXPosition).getLength();
        XYSeries data = dataProcessor.getProcessedNavigatorData(traceNumber, xLength, getNavigatorTraceMarkSize(traceNumber));
        navigator.setTraceData(traceNumber, data);
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
