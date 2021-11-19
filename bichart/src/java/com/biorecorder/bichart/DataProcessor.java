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
    private boolean isProcessingEnabled = true;
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
                XYSeries data = getProcessedChartData(traceNumber, range.getMin(), range.getMax(), xLength);
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
            XYSeries data = getProcessedNavigatorData(i,navigatorRange.getMin(), navigatorRange.getMax(), xLength);
            tracesData.put(i, data);
        }
        navTracesNeedUpdate = false;
        return tracesData;
    }

    // suppose that data is ordered
    private static Range dataRange(XYSeries data) {
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
        for (GroupedData groupedData : navigatorGroupedData) {
            if(groupedData != null) {
                groupedData.dataAppended();
            }
        }
    }

    private int getDataLength(XYSeries data, double min, double max, int minMaxLength) {
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(data.size() - 1);
        // prepare scale to calculate dataLength
        scale.setStartEnd(0, minMaxLength);
        scale.setMinMax(min, max);
        int dataLength = (int)(scale.scale(dataMax) - scale.scale(dataMin));
        if(dataLength < 1) {
            dataLength = 1;
        }
        return dataLength;
    }

    private XYSeries getProcessedNavigatorData(int traceNumber,  double min, double max, int minMaxLength) {
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        int markSize = navigatorTracesMarkSizes.get(traceNumber);
        if(groupedData != null) {
            return groupedData.getData(minMaxLength, markSize);
        }
        XYSeries rowData = navigatorData.get(traceNumber);
        if(rowData.size() > 1) {
            int dataLength = getDataLength(rowData, min, max, minMaxLength);
            if (isDateTime) {
                groupedData = groupDataWithTimeIntervals(rowData, config.getGroupingType(), dataLength, markSize, config.getGroupingTimeIntervals());
            } else {
                groupedData = groupDataWithIntervals(rowData, config.getGroupingType(),dataLength, markSize, config.getGroupingIntervals());
            }
            if(groupedData != null) {
                navigatorGroupedData.set(traceNumber, groupedData);
                return groupedData.getData(minMaxLength, markSize);
            }
        }
        return rowData;
    }

    private XYSeries getProcessedChartData(int traceNumber, double min, double max, int minMaxLength) {
        XYSeries data = chartData.get(traceNumber);
        if(data.size() < minPointsForCrop) {
            return data;
        }
        int markSize = chartTracesMarkSizes.get(traceNumber);
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(data.size() - 1);
        if(dataMin > max || dataMax < min) {
           return data.getEmptyCopy();
        }
        if(dataMax == dataMin) {
            return data;
        }
        // crop data
        int indexFrom = 0;
        int indexTill = data.size();
        if(dataMin < min) {
            indexFrom = data.bisectLeft(min);
        }
        if(dataMax > max) {
            indexTill = data.bisectRight(max);
        }

        int extraPoints = config.getCropShoulder();
        indexFrom -= extraPoints;
        indexTill += extraPoints;
        if(indexFrom < 0) {
            indexFrom = 0;
        }
        if(indexTill > data.size()) {
            indexTill = data.size();
        }
        int dataSize = indexTill - indexFrom;
        if(dataSize < data.size()) {
            data = data.view(indexFrom, indexTill - indexFrom);
        }
        if(data.size() <= 1) {
            return data;
        }
        int dataLength = getDataLength(data, min, max, minMaxLength);
        GroupedData groupedData = null;
        if (isDateTime) {
            groupedData = groupDataWithTimeIntervals(data, config.getGroupingType(), dataLength, markSize, null);
        } else {
            groupedData = groupDataWithIntervals(data, config.getGroupingType(),dataLength, markSize, null);
        }
        if(groupedData != null) {
            return groupedData.getData(minMaxLength, markSize);
        }
        return data;
    }

    private GroupedData groupDataWithTimeIntervals(XYSeries data, GroupingType groupingType, double dataLength, int markSize, TimeInterval[] timeIntervals) {
        int dataSize = data.size();
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(dataSize - 1);
        if(timeIntervals != null && timeIntervals.length != 0) {
            if(groupingType == GroupingType.EQUAL_INTERVALS) {
               return GroupedData.groupDataByTimeIntervals(data, timeIntervals);
            }
            if(groupingType == GroupingType.EQUAL_POINTS) {
                IntArrayList pointsList = new IntArrayList(timeIntervals.length);
                for (TimeInterval interval : timeIntervals) {
                    int points = intervalToPoints(dataMin, dataMax, dataSize, interval.toMilliseconds());
                    if(points > 1) {
                        pointsList.add(points);
                    }
                }
                if(pointsList.size() > 0) {
                    return GroupedData.groupDataByPoints(data, pointsList.toArray());
                }
            }
        } else {
            int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
            if(bestPoints > 1) {
                if(groupingType == GroupingType.EQUAL_INTERVALS) {
                    double bestInterval = bestGroupingInterval(dataMin, dataMax, dataLength, markSize);
                    TimeInterval bestTimeInterval = TimeInterval.getUpper((long)bestInterval, true);
                    return GroupedData.groupDataByTimeIntervals(data, bestTimeInterval);
                }
                if(groupingType == GroupingType.EQUAL_POINTS) {
                    return GroupedData.groupDataByPoints(data, bestPoints);
                }
            }
        }
        return null;
    }

    private GroupedData groupDataWithIntervals(XYSeries data, GroupingType groupingType, int dataLength, int markSize, double[] intervals) {
        int dataSize = data.size();
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(data.size() - 1);
        if(intervals != null && intervals.length != 0) {
            if(groupingType == GroupingType.EQUAL_INTERVALS) {
                return GroupedData.groupDataByIntervals(data, intervals);
            }
            if(groupingType == GroupingType.EQUAL_POINTS) {
                IntArrayList pointsList = new IntArrayList(intervals.length);
                for (double interval : intervals) {
                    int points = intervalToPoints(dataMin, dataMax, dataSize, interval);
                    if(points > 1) {
                        pointsList.add(points);
                    }
                }
                if(pointsList.size() > 0) {
                    return GroupedData.groupDataByPoints(data, pointsList.toArray());
                }
            }
        } else {
            int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
            if(bestPoints > 1) {
                if(groupingType == GroupingType.EQUAL_INTERVALS) {
                    double bestInterval = bestGroupingInterval(dataMin, dataMax, dataLength, markSize);
                    return GroupedData.groupDataByIntervals(data, bestInterval);
                }
                if(groupingType == GroupingType.EQUAL_POINTS) {
                    return GroupedData.groupDataByPoints(data, bestPoints);
                }
            }
        }
        return null;
    }

    private int intervalToPoints(double dataMin, double dataMax, int dataSize , double interval) {
        double groups = (dataMax - dataMin) / interval;
        if(groups < 1) {
            groups = 1;
        }
        int pointsPerGroup = (int)Math.round(dataSize / groups);
        return pointsPerGroup;
    }

    private static int bestPointsInGroup(int dataSize, double dataLength, int markSize) {
        return (int)Math.round(dataSize * markSize / dataLength);
    }

    static double bestGroupingInterval(double dataMin, double dataMax, double dataLength, int markSize) {
        if(dataMax == dataMin) {
            return 1;
        }
        return (dataMax - dataMin) * markSize / dataLength;
    }
}
