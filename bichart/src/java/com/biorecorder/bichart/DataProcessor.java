package com.biorecorder.bichart;

import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {
    private ProcessingConfig config;
    private boolean isDateTime;
    private int minPointsForCrop = 100;
    private List<XYSeries> chartData = new ArrayList<>();
    private List<XYSeries> navigatorData = new ArrayList<>();
    private List<GroupedData> navigatorGroupedData = new ArrayList<>();

    public DataProcessor(ProcessingConfig config, boolean isDateTime) {
        this.config = config;
        this.isDateTime = isDateTime;
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

    public Range getNavigatorTraceDataRange(int traceNumber) {
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData != null) {
            return groupedData.getDataRange();
        }
        return dataRange(navigatorData.get(traceNumber));
    }

    public void addChartTraceData(XYSeries data) {
        chartData.add(data);
    }

    public void addNavigatorTraceData(XYSeries data) {
        navigatorData.add(data);
        navigatorGroupedData.add(null);
    }

    public void removeNavigatorTraceData(int traceNumber) {
        navigatorData.remove(traceNumber);
        navigatorGroupedData.remove(traceNumber);
    }

    public void removeChartTraceData(int traceNumber) {
        chartData.remove(traceNumber);
    }

    public void appendChartTraceData(int traceNumber, XYSeries data) {
        chartData.get(traceNumber).appendData(data);
    }

    public void appendNavigatorTraceData(int traceNumber, XYSeries data) {
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData != null) {
            groupedData.appendData(data);
        } else {
            XYSeries rowData = navigatorData.get(traceNumber);
            rowData.appendData(data);
        }
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
        return groupedData.getData(xLength, markSize);
    }

    public XYSeries getProcessedChartData(int traceNumber, double min, double max, double xLength, int markSize) {
        XYSeries data = chartData.get(traceNumber);
        Range dataXRange = GroupedData.dataRange(data);
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

        return data;
    }
}
