package com.biorecorder.bichart;

import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcessor {
    private ProcessingConfig config;
    private boolean isProcessingEnabled = true;
    private boolean isDateTime;
    private int minPointsForProcessing = 10;
    private List<XYSeries> chartData = new ArrayList<>();
    private List<XYSeries> navigatorData = new ArrayList<>();
    private List<GroupedData> navigatorGroupedData = new ArrayList<>();

    private List<Integer> chartDataSizes = new ArrayList<>();
    private List<Integer> navigatorDataSizes = new ArrayList<>();

    private Scale scale;
    private int xLength;
    private Map<List<Integer>, Range> chartTracesToUpdate = new HashMap<>(2);
    private Range navigatorRange;
    private boolean navTracesNeedUpdate;

    public DataProcessor(boolean isDateTime, Scale scale, boolean isProcessingEnabled) {
        this.config = new ProcessingConfig();
        this.isDateTime = isDateTime;
        this.isProcessingEnabled = isProcessingEnabled;
        this.scale = scale;
    }

    public void onChartRangeChanged(double min, double max, List<Integer> traceNumbers) {
        chartTracesToUpdate.put(traceNumbers, new Range(min, max));
    }

    public void onNavigatorRangeChanged(double min, double max) {
        navTracesNeedUpdate = true;
        navigatorRange = new Range(min, max);
    }

    public void onResize(int xLength) {
        this.xLength = xLength;
        navTracesNeedUpdate = true;
    }

    public Map<Integer, XYSeries> chartTracesDataToUpdate() {
        if(!isProcessingEnabled || chartTracesToUpdate.keySet().isEmpty()) {
            return null;
        }
        HashMap<Integer, XYSeries> tracesData = new HashMap<>(chartData.size());
        for (List<Integer> tracesNumbers : chartTracesToUpdate.keySet()) {
            Range range = chartTracesToUpdate.get(tracesNumbers);
            scale.setMinMax(range.getMin(), range.getMax());
            scale.setStartEnd(0, xLength);
            for (Integer traceNumber : tracesNumbers) {
                XYSeries data = getProcessedChartData(traceNumber, range.getMin(), range.getMax(), xLength, chartDataSizes.get(traceNumber));
                tracesData.put(traceNumber, data);
            }
        }
        chartTracesToUpdate.clear();
        return tracesData;
    }

    public Map<Integer, XYSeries> navigatorTracesDataToUpdate() {
        if(!isProcessingEnabled || !navTracesNeedUpdate) {
            return null;
        }
        HashMap<Integer, XYSeries> tracesData = new HashMap<>(navigatorData.size());
        for (int i = 0; i < navigatorData.size(); i++) {
            XYSeries data = getProcessedNavigatorData(i,navigatorRange.getMin(), navigatorRange.getMax(), xLength, navigatorDataSizes.get(i));
            tracesData.put(i, data);
        }
        navTracesNeedUpdate = false;
        return tracesData;
    }

    // suppose that data is ordered
    private Range dataRange(XYSeries data) {
        if(data.size() > 0) {
            return new Range(data.getX(0), data.getX(data.size() - 1));
        }
        return null;
    }

    public Range getChartTraceDataRange(int traceNumber) {
        return dataRange(chartData.get(traceNumber));
    }

    public int getChartTraceDataSize(int traceNumber) {
        return chartData.get(traceNumber).size();
    }

    public Range getNavigatorTraceDataRange(int traceNumber) {
        return dataRange(navigatorData.get(traceNumber));
    }

    public void addChartTraceData(XYSeries data, int markSize) {
        chartData.add(data);
        chartDataSizes.add(markSize);
    }

    public void addNavigatorTraceData(XYSeries data, int markSize) {
        navigatorData.add(data);
        navigatorGroupedData.add(null);
        navigatorDataSizes.add(markSize);
    }

    public void removeNavigatorTraceData(int traceNumber) {
        navigatorData.remove(traceNumber);
        navigatorGroupedData.remove(traceNumber);
        navigatorDataSizes.remove(traceNumber);
    }

    public void removeChartTraceData(int traceNumber) {
        chartData.remove(traceNumber);
        chartDataSizes.remove(traceNumber);
    }

    public void dataAppended() {
        for (GroupedData groupedData : navigatorGroupedData) {
            if(groupedData != null) {
                groupedData.dataAppended();
            }
        }
    }

    private XYSeries getProcessedNavigatorData(int traceNumber,  double min, double max, double xLength, int markSize) {
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData == null) {
            XYSeries rowData = navigatorData.get(traceNumber);
            Range dataRange = dataRange(rowData);
            if(dataRange == null || dataRange.length() == 0 || rowData.size() < minPointsForProcessing) {
                return rowData;
            }
            // prepare scale to calculate dataLength
            scale.setStartEnd(0, xLength);
            scale.setMinMax(min, max);
            double dataLength = scale.scale(dataRange.getMax()) - scale.scale(dataRange.getMin());
            if(isDateTime) {
                groupedData = new GroupedData(rowData, config.getGroupingType(), getTimeGroupingIntervals(dataRange.getMin(), dataRange.getMax(), dataLength, markSize));
            } else {
                groupedData = new GroupedData(rowData, config.getGroupingType(), getGroupingIntervals(dataRange.getMin(), dataRange.getMax(), dataLength, markSize));
            }
            navigatorGroupedData.set(traceNumber, groupedData);
        }
        return groupedData.getData(xLength, markSize);
    }

    private XYSeries getProcessedChartData(int traceNumber, double min, double max, double xLength, int markSize) {
        XYSeries data = chartData.get(traceNumber);
        Range dataRange = dataRange(data);
        if(dataRange == null || dataRange.length() == 0 || data.size() < minPointsForProcessing) {
            return data;
        }
        if(dataRange.getMin() > max || dataRange.getMax() < min) {
           return data.getEmptyCopy();
        }
        // prepare scale to calculate dataLength
        scale.setStartEnd(0, xLength);
        scale.setMinMax(min, max);
        // crop data and calculate dataLength
        int indexFrom = 0;
        int indexTill = data.size();
        double dataMin = dataRange.getMin();
        double dataMax = dataRange.getMax();
        if(dataMin < min) {
            indexFrom = data.bisectLeft(min);
            dataMin = min;
        }
        if(dataMax > max) {
            indexTill = data.bisectRight(max);
            dataMax = max;
        }
        double dataLength = scale.scale(dataMax) - scale.scale(dataMin);
        int pointsPerGroup = 1;
        // calculate extra points for CropShoulder
        int dataSize = indexTill - indexFrom;
        pointsPerGroup = bestPointsInGroup(dataSize, dataLength, markSize);
        int extraPoints = pointsPerGroup * config.getCropShoulder();
        indexFrom -= extraPoints;
        indexTill += extraPoints;
        if(indexFrom < 0) {
            indexFrom = 0;
        }
        if(indexTill > data.size()) {
            indexTill = data.size();
        }
        dataSize = indexTill - indexFrom;
        if(dataSize < data.size()) {
            data = data.view(indexFrom, indexTill - indexFrom);
        }
        // group cropped data
        if(pointsPerGroup > 1) {
            if(isDateTime) {
                data = new GroupedData(data, config.getGroupingType(), bestTimeGroupingInterval(dataMin, dataMax, dataLength, markSize)).getData(dataLength, markSize);
            } else {
                data = new GroupedData(data, config.getGroupingType(), bestGroupingInterval(dataMin, dataMax, dataLength, markSize)).getData(dataLength, markSize);
            }
        }
        return data;
    }

    private int bestPointsInGroup(int dataSize, double xLength, int markSize) {
        return (int)Math.round(dataSize * markSize / xLength);
    }

    private double[] getGroupingIntervals(double dataMin, double dataMax, double dataLength, int markSize) {
        double[] intervals = config.getGroupingIntervals();
        if(intervals == null || intervals.length == 0) {
            intervals = new double[1];
            intervals[0] = bestGroupingInterval(dataMin, dataMax, dataLength, markSize);
        }
        return intervals;
    }

    private TimeInterval[] getTimeGroupingIntervals(double dataMin, double dataMax, double dataLength, int markSize) {
        TimeInterval[] timeIntervals = config.getGroupingTimeIntervals();
        if(timeIntervals == null || timeIntervals.length == 0) {
            timeIntervals = new TimeInterval[1];
            timeIntervals[0] = bestTimeGroupingInterval(dataMin, dataMax, dataLength, markSize);
        }
        return timeIntervals;
    }

    static TimeInterval bestTimeGroupingInterval(double dataMin, double dataMax, double dataLength, int markSize) {
        return TimeInterval.getUpper(Math.round(bestGroupingInterval(dataMin, dataMax, dataLength, markSize)), false);

    }

    static double bestGroupingInterval(double dataMin, double dataMax, double dataLength, int markSize) {
        return (dataMax - dataMin) * markSize / dataLength;
    }
}
