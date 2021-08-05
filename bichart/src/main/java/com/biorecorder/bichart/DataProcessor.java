package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame_new.aggregation.TimeInterval;

import java.util.ArrayList;
import java.util.List;


public class DataProcessor {
    private ProcessingConfig config;
    private boolean isDateTime;
    private int minPointsForCrop = 100;
    private List<XYData> chartData = new ArrayList<>();
    private List<GroupedData> navigatorGroupedData = new ArrayList<>();

    public DataProcessor(ProcessingConfig config, boolean isDateTime) {
        this.config = config;
        this.isDateTime = isDateTime;
    }

    public void addChartTrace(XYData data) {
        chartData.add(data);
    }

    private GroupedData addNavigatorTrace(XYData data, int xLength, int markSize) {
        if(isDateTime) {
            return new GroupedData(data, config.getGroupingType(), config.getGroupingTimeIntervals(), xLength, markSize);
        } else {
            return new GroupedData(data, config.getGroupingType(), config.getGroupingIntervals(), xLength, markSize);
       }
    }

    public void appendNavigatorTraceData(int traceNumber, XYData data) {
        navigatorGroupedData.get(traceNumber).appendData(data);
    }

    public XYData getProcessedNavigatorData(int traceNumber, int xLength, int markSize) {
        return navigatorGroupedData.get(traceNumber).getData(xLength, markSize);
    }

    public XYData getProcessedChartData(int traceNumber, Range xMinMax, int xLength, int markSize) {
        XYData data = chartData.get(traceNumber);
        Range dataXRange = GroupedData.dataRange(data);
        if(!config.isDataCropEnabled() || dataXRange == null ||
              xMinMax.contains(dataXRange)  || data.rowCount() < minPointsForCrop) {
            return data;
        }
        int pointsPerGroup = 1;
        if(config.isDataGroupingEnabled()) {
            pointsPerGroup = GroupedData.bestPointsInGroup(data, xLength, markSize) + 1;
        }
        int indexFrom = data.bisectLeft(dataXRange.getMin());
        int indexTill = data.bisectRight(dataXRange.getMax());
        int extraPoints = pointsPerGroup * config.getCropShoulder();
        indexFrom -= extraPoints;
        indexTill += extraPoints;
        if(indexFrom < 0) {
            indexFrom = 0;
        }
        if(indexTill > data.rowCount()) {
            indexTill = data.rowCount();
        }
        XYData resultantData = data.view(indexFrom, indexTill - indexFrom);
        if(config.isDataGroupingEnabled()) {
            if(isDateTime) {
                resultantData = new GroupedData(resultantData, config.getGroupingType(), new TimeInterval[0], xLength, markSize).getData(xLength, markSize);
            } else {
                resultantData = new GroupedData(resultantData, config.getGroupingType(), new double[0], xLength, markSize).getData(xLength, markSize);
            }
        }
        return resultantData;
    }
}
