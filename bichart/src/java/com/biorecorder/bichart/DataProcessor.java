package com.biorecorder.bichart;

import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {
    private ProcessingConfig config;
    private boolean isProcessingEnabled = true;
    private boolean isDateTime;
    private int minPointsForCrop = 10;
    private List<Boolean> chartTraceNeedUpdateDataFlags = new ArrayList<>();
    private List<Boolean> navTraceNeedUpdateDataFlags = new ArrayList<>();
    private List<XYSeries> chartData = new ArrayList<>();
    private List<XYSeries> navigatorData = new ArrayList<>();
    private List<GroupedData> navigatorGroupedData = new ArrayList<>();

    public DataProcessor(boolean isDateTime, boolean isProcessingEnabled) {
        this.config = new ProcessingConfig();
        this.isDateTime = isDateTime;
        this.isProcessingEnabled = isProcessingEnabled;
    }

    public void onChartRangeChanged(double min, double max, List<Integer> traceNumbers) {
        for (int i = 0; i < traceNumbers.size(); i++) {
            int traceNumber = traceNumbers.get(i);
            chartTraceNeedUpdateDataFlags.set(traceNumber, true);
        }
    }

    public void onNavigatorRangeChanged(double min, double max) {
        for (int i = 0; i < navTraceNeedUpdateDataFlags.size(); i++) {
            navTraceNeedUpdateDataFlags.set(i, true);
        }
    }

    public void onResize(int xLength) {
        for (int i = 0; i < chartTraceNeedUpdateDataFlags.size(); i++) {
            chartTraceNeedUpdateDataFlags.set(i, true);
        }
        for (int i = 0; i < navTraceNeedUpdateDataFlags.size(); i++) {
            navTraceNeedUpdateDataFlags.set(i, true);
        }
    }

    public boolean isChartTraceNeedData(int traceNumber) {
        if(!isProcessingEnabled) {
            return false;
        }
        return chartTraceNeedUpdateDataFlags.get(traceNumber);
    }

    public boolean isNavigatorTraceNeedData(int traceNumber) {
        if(!isProcessingEnabled) {
            return false;
        }
        return navTraceNeedUpdateDataFlags.get(traceNumber);
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
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData != null) {
            return groupedData.getDataRange();
        }
        return dataRange(navigatorData.get(traceNumber));
    }

    public void addChartTraceData(XYSeries data) {
        chartData.add(data);
        chartTraceNeedUpdateDataFlags.add(true);
    }

    public void addNavigatorTraceData(XYSeries data) {
        navigatorData.add(data);
        navigatorGroupedData.add(null);
        navTraceNeedUpdateDataFlags.add(true);
    }

    public void removeNavigatorTraceData(int traceNumber) {
        navigatorData.remove(traceNumber);
        navigatorGroupedData.remove(traceNumber);
        navTraceNeedUpdateDataFlags.remove(traceNumber);
    }

    public void removeChartTraceData(int traceNumber) {
        chartData.remove(traceNumber);
        chartTraceNeedUpdateDataFlags.remove(traceNumber);
    }

    public void dataAppended() {
        for (GroupedData groupedData : navigatorGroupedData) {
            if(groupedData != null) {
                groupedData.dataAppended();
            }
        }
    }

    public XYSeries getProcessedNavigatorData(int traceNumber, double xLength, int markSize) {
        if(!config.isDataGroupingEnabled()) {
            return navigatorData.get(traceNumber);
        }
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData == null) {
            XYSeries rowData = navigatorData.get(traceNumber);
            if(isDateTime) {
                groupedData = new GroupedData(rowData, config.getGroupingType(), config.getGroupingTimeIntervals(), xLength, markSize);
            } else {
                groupedData = new GroupedData(rowData, config.getGroupingType(), config.getGroupingIntervals(), xLength, markSize);
            }
            navigatorGroupedData.set(traceNumber, groupedData);
            navigatorData.set(traceNumber, null);
        }
        navTraceNeedUpdateDataFlags.set(traceNumber, false);
        return groupedData.getData(xLength, markSize);
    }

    public XYSeries getProcessedChartData(int traceNumber, double min, double max, double xLength, int markSize) {
        XYSeries data = chartData.get(traceNumber);
        Range dataXRange = dataRange(data);
        int pointsPerGroup = 1;
        if(config.isDataCropEnabled() && dataXRange != null && (dataXRange.getMin() < min
                || dataXRange.getMax() > max) && data.size() > minPointsForCrop) {
            int indexFrom = data.bisectLeft(min);
            int indexTill = data.bisectRight(max);
            if(indexFrom == indexTill || indexFrom >= data.size() || indexTill < 0) {
                return data.getEmptyCopy();
            }
            if(config.isDataGroupingEnabled()) {
                int dataSize = indexTill - indexFrom;
                if(dataSize > 0) {
                    pointsPerGroup = GroupedData.bestPointsInGroup(dataSize, xLength, markSize);
                }
            }
            int extraPoints = pointsPerGroup * config.getCropShoulder();
            indexFrom -= extraPoints;
            indexTill += extraPoints;
            if(indexFrom < 0) {
                indexFrom = 0;
            }
            if(indexTill > data.size()) {
                indexTill = data.size();
            }
            data = data.view(indexFrom, indexTill - indexFrom);
        }
        if(config.isDataGroupingEnabled() && pointsPerGroup > 1) {
            if(isDateTime) {
                data = new GroupedData(data, config.getGroupingType(), new TimeInterval[0], xLength, markSize).getData(xLength, markSize);
            } else {
                data = new GroupedData(data, config.getGroupingType(), new double[0], xLength, markSize).getData(xLength, markSize);
            }
        }
        chartTraceNeedUpdateDataFlags.set(traceNumber, false);
        return data;
    }
}
