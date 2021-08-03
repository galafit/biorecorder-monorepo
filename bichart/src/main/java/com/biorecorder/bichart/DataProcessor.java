package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.data.frame_new.DataTable;
import com.biorecorder.data.frame_new.aggregation.Resampler;
import com.biorecorder.data.frame_new.aggregation.TimeInterval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DataProcessor {
    private List<XYData> navigatorRowData = new ArrayList<>();
    private List<XYData> chartRowData = new ArrayList<>();
    private ProcessingConfig processingConfig;
    private boolean isDateTime;
    private List<TraceData> chartData = new ArrayList<>();
    private List<TraceData> navigatorData = new ArrayList<>();

    public DataProcessor(ProcessingConfig processingConfig, boolean isDateTime) {
        this.processingConfig = processingConfig;
        this.isDateTime = isDateTime;
    }

    public Range getNavigatorTraceRange(int traceNumber) {
         return navigatorRowData.get(traceNumber).xMinMax();
    }

    public Range getChartTraceRange(int traceNumber) {
        return chartRowData.get(traceNumber).xMinMax();
    }


    public Range getDataBestRange(Range dataRange, int dataSize, int markSize, Scale xScale, int xLength, double min) {
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

    private Range getBestRange(List<XYData> dataList, List<Integer> markSizes, Scale xScale, boolean isFullRangeIncluded) {
        List<Range> rangeList = new ArrayList<>(dataList.size());
        Range minMax = null;
        for (XYData data : dataList) {
            Range dataMinMax = data.xMinMax();
            minMax = Range.join(minMax, dataMinMax);
            rangeList.add(dataMinMax);
        }
        if(minMax != null) {
            int xLength = (int) Math.round(xScale.getLength());
            for (int i = 0; i < dataList.size(); i++) {
                Range dataBestRange = getDataBestRange(rangeList.get(i), dataList.get(i).rowCount(), markSizes.get(i), xScale, xLength, minMax.getMin());
                if(dataBestRange != null && dataBestRange.length() > 0) {
                    if((isFullRangeIncluded && dataBestRange.getMax() > minMax.getMax()) ||
                            (isFullRangeIncluded && dataBestRange.getMax() > minMax.getMax())) {
                        minMax = new Range(minMax.getMin(), dataBestRange.getMax());
                    }
                }
            }
        }
        return minMax;
    }

    public Range getNavigatorTracesBestRange(Scale xScale, List<Integer> markSizes) {
        return getBestRange(navigatorRowData, markSizes, xScale, true);
    }

    public Range getChartTracesBestRange(Scale xScale, List<Integer> traceNumbers, Map<Integer, Integer> dataNumbersToMarkSizes) {
        List<XYData> dataList = new ArrayList<>(traceNumbers.size());
        List<Integer> markSizes = new ArrayList<>(traceNumbers.size());
        for (Integer traceNumber : traceNumbers) {
            dataList.add(chartRowData.get(traceNumber));
            markSizes.add(dataNumbersToMarkSizes.get(traceNumber));
        }
        return getBestRange(chartRowData, markSizes, xScale, false);
    }

    public void addChartTrace(XYData data) {
        chartRowData.add(data);
        chartData.add(null);
    }

    public void addNavigatorTrace(XYData data) {
        navigatorRowData.add(data);
        navigatorData.add(null);
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

    public void appendNavigatorTraceData(int traceNumber, XYData data) {
        navigatorData.get(traceNumber).appendData(data);
    }

    public XYData getChartData(int traceNumber, Range xMinMax, int xLength, int markSize) {
        TraceData traceData = chartData.get(traceNumber);
        if(traceData == null) {
            XYData rowData = chartRowData.get(traceNumber);
            traceData = new TraceData(rowData);
            chartData.set(traceNumber, traceData);
        }

        XYData fullData = traceData.getData(xMinMax, xLength, markSize);
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
        TraceData traceData = navigatorData.get(traceNumber);
        if(traceData == null) {
            XYData rowData = navigatorRowData.get(traceNumber);
            traceData = createNavigatorTraceData(rowData, xLength, markSize);
            navigatorData.set(traceNumber, traceData);
        }
        return traceData.getData(xMinMax, xLength, markSize);
    }

    private double calculateGroupingInterval(Range xMinMax, int xLength, int markSize) {
        return  xMinMax.length() * markSize / xLength;
    }
}
