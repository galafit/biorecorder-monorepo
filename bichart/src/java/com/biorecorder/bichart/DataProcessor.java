package com.biorecorder.bichart;

import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcessor {
    private ProcessingConfig config;
    private boolean isDateTime;
    private int minPointsForCrop = 10;
    private List<XYSeries> chartData = new ArrayList<>();
    private List<XYSeries> navigatorData = new ArrayList<>();
    private List<GroupedData> navigatorGroupedData = new ArrayList<>();

    private List<Integer> chartTracesMarkSizes = new ArrayList<>();
    private List<Integer> navigatorTracesMarkSizes = new ArrayList<>();

    private Scale scale;
    private int xLength;
    private Map<List<Integer>, Range> chartTracesToUpdate = new HashMap<>(2);
    private Range navigatorRange;
    private boolean navTracesNeedUpdate;

    public DataProcessor(boolean isDateTime, Scale scale, ProcessingConfig config) {
        this.config = config;
        this.isDateTime = isDateTime;
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
        if (!config.isProcessingEnabled() || chartTracesToUpdate.keySet().isEmpty()) {
            return null;
        }
        HashMap<Integer, XYSeries> tracesData = new HashMap<>(chartData.size());
        for (List<Integer> tracesNumbers : chartTracesToUpdate.keySet()) {
            Range range = chartTracesToUpdate.get(tracesNumbers);
            scale.setMinMax(range.getMin(), range.getMax());
            scale.setStartEnd(0, xLength);
            for (Integer traceNumber : tracesNumbers) {
                XYSeries data = getProcessedChartData(traceNumber, range.getMin(), range.getMax(), xLength);
                tracesData.put(traceNumber, data);
            }
        }
        chartTracesToUpdate.clear();
        return tracesData;
    }

    public Map<Integer, XYSeries> navigatorTracesDataToUpdate() {
        if (!config.isProcessingEnabled() || !navTracesNeedUpdate) {
            return null;
        }
        if(navigatorRange != null) {
            HashMap<Integer, XYSeries> tracesData = new HashMap<>(navigatorData.size());
            for (int i = 0; i < navigatorData.size(); i++) {
                XYSeries data = getProcessedNavigatorData(i, navigatorRange.getMin(), navigatorRange.getMax(), xLength);
                tracesData.put(i, data);
            }
            navTracesNeedUpdate = false;
            return tracesData;
        }
        return null;

    }

    // suppose that data is ordered
    private static Range dataRange(XYSeries data) {
        if (data.size() > 0) {
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
        chartTracesMarkSizes.add(markSize);
    }

    public void addNavigatorTraceData(XYSeries data, int markSize) {
        navigatorData.add(data);
        navigatorGroupedData.add(null);
        navigatorTracesMarkSizes.add(markSize);
    }

    public void removeNavigatorTraceData(int traceNumber) {
        navigatorData.remove(traceNumber);
        navigatorGroupedData.remove(traceNumber);
        navigatorTracesMarkSizes.remove(traceNumber);
    }

    public void removeChartTraceData(int traceNumber) {
        chartData.remove(traceNumber);
        chartTracesMarkSizes.remove(traceNumber);
    }

    public void dataAppended() {
        for (int i = 0; i < navigatorData.size(); i++) {
            XYSeries navData = navigatorData.get(i);
            navData.updateSize();
            GroupedData groupedData = navigatorGroupedData.get(i);
            if (groupedData != null) {
                int from = groupedData.processedSampleCount();
                int length = navData.size() - from;
                groupedData.appendData(navData, from, length);
            }
        }
        for (int i = 0; i < chartData.size(); i++) {
           chartData.get(i).updateSize();
        }
    }

    private XYSeries getProcessedChartData(int traceNumber, double min, double max, int minMaxLength) {
        XYSeries data = chartData.get(traceNumber);
        if(config.isCropEnabled()) {
            data = cropData(data, min, max);
        }
        if (!config.isGroupingEnabled() || data.size() <= 1 ) {
            return data;
        }
        int markSize = chartTracesMarkSizes.get(traceNumber);
        GroupedData groupedData = groupData(data, markSize, min, max, minMaxLength, null, null);
        if (groupedData != null) {
            return groupedData.getData(minMaxLength, markSize);
        }
        return data;
    }

    private XYSeries getProcessedNavigatorData(int traceNumber, double min, double max, int minMaxLength) {
        if(!config.isGroupingEnabled()) {
           return navigatorData.get(traceNumber);
        }
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        int markSize = navigatorTracesMarkSizes.get(traceNumber);
        if (groupedData != null) {
            return groupedData.getData(minMaxLength, markSize);
        }
        XYSeries rowData = navigatorData.get(traceNumber);
        groupedData = groupData(rowData, markSize, min, max, minMaxLength, config.getGroupingIntervals(), config.getGroupingTimeIntervals());
        if (groupedData != null) {
            navigatorGroupedData.set(traceNumber, groupedData);
            return groupedData.getData(minMaxLength, markSize);
        }
        return rowData;
    }

    private XYSeries cropData(XYSeries data, double min, double max) {
        if (data.size() < minPointsForCrop) {
            return data;
        }
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(data.size() - 1);
        if (dataMin > max || dataMax < min) {
            return data.getEmptyCopy();
        }
        if (dataMax == dataMin) {
            return data;
        }
        // crop data
        int indexFrom = 0;
        int indexTill = data.size();
        if (dataMin < min) {
            indexFrom = data.bisectLeft(min);
        }
        if (dataMax > max) {
            indexTill = data.bisectRight(max);
        }

        int extraPoints = config.getCropShoulder();
        indexFrom -= extraPoints;
        indexTill += extraPoints;
        if (indexFrom < 0) {
            indexFrom = 0;
        }
        if (indexTill > data.size()) {
            indexTill = data.size();
        }
        return data.view(indexFrom, indexTill - indexFrom);
    }

    private GroupedData groupData(XYSeries data, int markSize, double min, double max, int minMaxLength, double[] intervals, TimeInterval[] timeIntervals) {
        int dataSize = data.size();
        if(dataSize <= 1) {
            return null;
        }
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(dataSize - 1);
        int dataLength = getDataLength(dataMin, dataMax, min, max, minMaxLength);
        if (config.getGroupingType() == GroupingType.EQUAL_INTERVALS) {
            if(isDateTime) {
                TimeInterval[] timeIntervals1 = normalizeTimeIntervals(timeIntervals, dataMin, dataMax, dataSize, dataLength, markSize);
                if (timeIntervals1 != null && timeIntervals1.length > 0) {
                    return GroupedData.groupDataByTimeIntervals(data, timeIntervals);
                }
            } else {
                double[] intervals1 = normalizeIntervals(intervals, dataMin, dataMax, dataSize, dataLength, markSize);
                if (intervals1 != null && intervals1.length > 0) {
                    return GroupedData.groupDataByIntervals(data, intervals);
                }
            }
        }

        if (config.getGroupingType() == GroupingType.EQUAL_POINTS) {
            IntArrayList pointsList = new IntArrayList();
            boolean intervalsNotNull = false;
            if(isDateTime) {
                if(timeIntervals !=null && timeIntervals.length > 0){
                    intervalsNotNull = true;
                    for (TimeInterval timeInterval : timeIntervals) {
                        int points = intervalToPoints(dataMin, dataMax, dataSize, timeInterval.toMilliseconds());
                        if(points > 1) {
                            pointsList.add(points);
                        }
                    }
                }
            } else {
                if(intervals != null && intervals.length > 0) {
                    intervalsNotNull = true;
                    for (double interval : intervals) {
                        int points = intervalToPoints(dataMin, dataMax, dataSize, interval);
                        if(points > 1) {
                            pointsList.add(points);
                        }
                    }
                }
            }

            if(intervalsNotNull == false) {
                int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
                if (bestPoints > 1) {
                    pointsList.add(bestPoints);
                }
            }
            if(pointsList.size() != 0) {
                return GroupedData.groupDataByPoints(data, pointsList.toArray());
            }
        }
        return null;
    }

    private int getDataLength(double dataMin,double dataMax,  double min, double max, int minMaxLength) {
        // prepare scale to calculate dataLength
        scale.setStartEnd(0, minMaxLength);
        scale.setMinMax(min, max);
        int dataLength = (int) (scale.scale(dataMax) - scale.scale(dataMin));
        if (dataLength < 1) {
            dataLength = 1;
        }
        return dataLength;
    }

    private double[] normalizeIntervals(double[] intervals, double dataMin, double dataMax, int dataSize, int dataLength, int markSize) {
        if (intervals != null && intervals.length != 0) {
            return intervals;
        }
        int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
        if (bestPoints > 1) {
            double bestInterval = bestGroupingInterval(dataMin, dataMax, dataLength, markSize);
            double[] bestIntervals = {bestInterval};
            return bestIntervals;
        }
        return null;
    }

    private TimeInterval[] normalizeTimeIntervals(TimeInterval[] timeIntervals, double dataMin, double dataMax, int dataSize, int dataLength, int markSize) {
        if (timeIntervals != null && timeIntervals.length != 0) {
            return timeIntervals;
        }
        int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
        if (bestPoints > 1) {
            double bestInterval = bestGroupingInterval(dataMin, dataMax, dataLength, markSize);
            TimeInterval[] bestIntervals = {TimeInterval.getUpper((long) bestInterval, true)};
            return bestIntervals;
        }
        return null;
    }


    private int intervalToPoints(double dataMin, double dataMax, int dataSize, double interval) {
        int pointsPerGroup = (int) Math.round(interval *  dataSize / (dataMax - dataMin));
        return pointsPerGroup;
    }

    private int bestPointsInGroup(int dataSize, double dataLength, int markSize) {
        return (int) Math.round(dataSize * markSize / dataLength);
    }

    private double bestGroupingInterval(double dataMin, double dataMax, double dataLength, int markSize) {
        if (dataMax == dataMin) {
            return 1;
        }
        return (dataMax - dataMin) * markSize / dataLength;
    }
}
