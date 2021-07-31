package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame_new.DataTable;
import com.biorecorder.data.frame_new.aggregation.Resampler;
import com.biorecorder.data.frame_new.aggregation.TimeInterval;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {
    private ProcessingConfig processingConfig;
    private boolean isDateTime;
    private List<TraceData> chartDataList = new ArrayList<>();
    private List<TraceData> navigatorDataList = new ArrayList<>();

    public DataProcessor(ProcessingConfig processingConfig, boolean isDateTime) {
        this.processingConfig = processingConfig;
        this.isDateTime = isDateTime;
    }

    public void appendNavigatorTraceData(int traceNumber, XYData data) {
        navigatorDataList.get(traceNumber).appendData(data);
    }

    public XYData getChartData(int traceNumber, Range xMinMax, int xLength, int markSize) {
        XYData fullData = chartDataList.get(traceNumber).getData(xMinMax, xLength, markSize);
        double dataAvgStep = fullData.dataAvgStep();
        double groupingInterval = calculateGroupingInterval(xMinMax, xLength, markSize) / dataAvgStep;
        int dataPointsPerInterval = (int) Math.round(groupingInterval);
        if(dataPointsPerInterval < 1) {
            dataPointsPerInterval = 1;
        }
        int indexFrom = fullData.bisectLeft(xMinMax.getMin());
        int indexTill = fullData.bisectRight(xMinMax.getMax());
        indexFrom -= dataPointsPerInterval;
        indexTill += dataPointsPerInterval;
        if(indexFrom < 0) {
            indexFrom = 0;
        }
        if(indexTill >= fullData.rowCount()) {
            indexTill = fullData.rowCount();
        }
        XYData resultantData = fullData.view(indexFrom, indexTill - indexFrom);
        if(dataPointsPerInterval > 1) {
            Resampler resampler;
            if(processingConfig.getGroupingType() == GroupingType.EQUAL_POINTS) {
                resampler = Resampler.createEqualPointsResampler(dataPointsPerInterval);
            } else {
                if(isDateTime) {
                   resampler = Resampler.createEqualTimeIntervalResampler(TimeInterval.getUpper((long)groupingInterval, false));
                } else {
                    resampler = Resampler.createEqualIntervalResampler(groupingInterval);
                }
            }
            DataTable dt = resampler.resampleAndAppend(resultantData.getDataTable());
            resultantData = new XYData(dt);
        }
        return resultantData;
    }

    public XYData getNavigatorData(int traceNumber, Range xMinMax, int xLength, int markSize) {
        return navigatorDataList.get(traceNumber).getData(xMinMax, xLength, markSize);
    }

    public void addChartTrace(XYData data) {
        chartDataList.add(new TraceData(data));
    }

    public void setChartTraceData(int traceNumber, XYData data) {
        chartDataList.set(traceNumber, new TraceData(data));
    }

    private TraceData createNavigatorTraceData(XYData data, int xLength,  int markSize) {
        if(isDateTime) {
            TimeInterval[] timeIntervals = processingConfig.getGroupingTimeIntervals();
            if(timeIntervals == null) {
                long interval = (long) calculateGroupingInterval(data.xMinMax(), xLength, markSize);
                TimeInterval timeInterval = TimeInterval.getUpper(interval, false);
                timeIntervals = new TimeInterval[1];
                timeIntervals[0] = timeInterval;
            }
            return new TraceData(data, processingConfig.getGroupingType(), timeIntervals);
        } else {
            double[] intervals = processingConfig.getGroupingIntervals();
            if(intervals == null) {
                double interval =  calculateGroupingInterval(data.xMinMax(), xLength, markSize);
                intervals = new double[1];
                intervals[0] = interval;
            }
            return new TraceData(data, processingConfig.getGroupingType(), intervals);
        }
    }

    public void addNavigatorTrace(XYData data, int xLength,  int markSize) {
        navigatorDataList.add(createNavigatorTraceData(data, xLength, markSize));
    }

    public void setNavigatorTraceData(int traceNumber, XYData data, int xLength,  int markSize) {
        navigatorDataList.set(traceNumber, createNavigatorTraceData(data, xLength, markSize));
    }

    private double calculateGroupingInterval(Range xMinMax, int xLength, int markSize) {
        return  xMinMax.length() * markSize / xLength;
    }
}
