package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame_new.DataTable;
import com.biorecorder.data.frame_new.aggregation.TimeInterval;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {
    private ProcessingConfig config;
    private boolean isDateTime;
    private int minPointsForCrop = 100;
    private List<XYData> chartRowData = new ArrayList<>();
    private List<XYData> navigatorRowData = new ArrayList<>();
    private List<GroupedData> navigatorGroupedData = new ArrayList<>();

    public DataProcessor(ProcessingConfig config, boolean isDateTime) {
        this.config = config;
        this.isDateTime = isDateTime;
    }

    public boolean isDateTime() {
        return isDateTime;
    }

    public void addChartTraceData(XYData data) {
        chartRowData.add(data);
    }

    public void addNavigatorTraceData(XYData data) {
       navigatorRowData.add(data);
       navigatorGroupedData.add(null);
    }

    public void setChartTraceData(int traceNumber, XYData data) {
        chartRowData.set(traceNumber, data);
    }

    public void appendNavigatorTraceData(int traceNumber, XYData data) {
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData != null) {
            groupedData.appendData(data);
        } else {
            XYData rowData = navigatorRowData.get(traceNumber);
            rowData.appendData(data);
        }
    }

    public void appendChartTraceData(int traceNumber, XYData dataToAppend) {
        XYData data = chartRowData.get(traceNumber);
        data.appendData(dataToAppend);
    }

    public XYData getProcessedNavigatorData(int traceNumber, int xLength, int markSize) {
        if(!config.isDataGroupingEnabled()) {
            return navigatorRowData.get(traceNumber);
        }
        GroupedData groupedData = navigatorGroupedData.get(traceNumber);
        if(groupedData == null) {
            XYData rowData = navigatorRowData.get(traceNumber);
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

    public XYData getProcessedChartData(int traceNumber, Range xMinMax, int xLength, int markSize) {
        XYData data = chartRowData.get(traceNumber);
        Range dataXRange = GroupedData.dataRange(data);
        int pointsPerGroup = 1;
        if(config.isDataCropEnabled() && dataXRange != null &&
              !xMinMax.contains(dataXRange) && data.rowCount() > minPointsForCrop) {
            int indexFrom = data.bisectLeft(xMinMax.getMin());
            int indexTill = data.bisectRight(xMinMax.getMax());

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
            if(indexTill > data.rowCount()) {
                indexTill = data.rowCount();
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

    public XYData getChartRowData(int traceNumber) {
        return chartRowData.get(traceNumber);
    }
}
