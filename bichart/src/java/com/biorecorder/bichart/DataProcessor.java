package com.biorecorder.bichart;

import com.biorecorder.bichart.configs.ProcessingConfig;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {
    private ProcessingConfig config;
    private boolean isDateTime;
    private int minPointsForCrop = 100;
    private List<XYSeries> chartRowData = new ArrayList<>();
    private List<XYSeries> navigatorRowData = new ArrayList<>();
    private List<GroupedData> navigatorGroupedData = new ArrayList<>();

    public DataProcessor(ProcessingConfig config, boolean isDateTime) {
        this.config = config;
        this.isDateTime = isDateTime;
    }

    public boolean isDateTime() {
        return isDateTime;
    }

    public void addChartTraceData(XYSeries data) {
        chartRowData.add(data);
    }

    public void addNavigatorTraceData(XYSeries data) {
        navigatorRowData.add(data);
        navigatorGroupedData.add(null);
    }

    public void removeNavigatorTraceData(int traceNumber) {
        navigatorRowData.remove(traceNumber);
        navigatorGroupedData.remove(traceNumber);
    }

    public void removeChartTraceData(int traceNumber) {
        chartRowData.remove(traceNumber);
    }

    public void setChartTraceData(int traceNumber, XYSeries data) {
        chartRowData.set(traceNumber, data);
    }

    public void setNavigatorTraceData(int traceNumber, XYSeries data) {
        navigatorRowData.set(traceNumber, data);
        navigatorGroupedData.set(traceNumber, null);
    }

    public void appendNavigatorTraceData(int traceNumber, XYSeries data) {
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData != null) {
            groupedData.appendData(data);
        } else {
            XYSeries rowData = navigatorRowData.get(traceNumber);
            rowData.appendData(data);
        }
    }

    public void appendChartTraceData(int traceNumber, XYSeries dataToAppend) {
        XYSeries data = chartRowData.get(traceNumber);
        data.appendData(dataToAppend);
    }

    public XYSeries getProcessedNavigatorData(int traceNumber, double xLength, int markSize) {
        if(!config.isDataGroupingEnabled()) {
            return navigatorRowData.get(traceNumber);
        }
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData == null) {
            XYSeries rowData = navigatorRowData.get(traceNumber);
            if(isDateTime) {
                groupedData = new GroupedData(rowData, config.getGroupingType(), config.getGroupingTimeIntervals(), xLength, markSize);
            } else {
                groupedData = new GroupedData(rowData, config.getGroupingType(), config.getGroupingIntervals(), xLength, markSize);
            }
            navigatorGroupedData.set(traceNumber, groupedData);
            navigatorRowData.set(traceNumber, null);
        }
        return groupedData.getData(xLength, markSize);
    }

    public XYSeries getProcessedChartData(int traceNumber, Range xMinMax, double xLength, int markSize) {
        XYSeries data = chartRowData.get(traceNumber);
        Range dataXRange = GroupedData.dataRange(data);
        int pointsPerGroup = 1;
        if(config.isDataCropEnabled() && dataXRange != null &&
                !xMinMax.contain(dataXRange) && data.size() > minPointsForCrop) {
            int indexFrom = data.bisectLeft(xMinMax.getMin());
            int indexTill = data.bisectRight(xMinMax.getMax());
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
            data = data.view(indexFrom, indexTill - indexFrom + 1);
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

    public XYSeries getChartRowData(int traceNumber) {
        return chartRowData.get(traceNumber);
    }
}
