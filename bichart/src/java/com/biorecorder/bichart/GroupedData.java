package com.biorecorder.bichart;

import com.biorecorder.datalyb.datatable.aggregation.Resampler;
import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.List;

class GroupedData {
    List<ResampledData> dataList = new ArrayList<>();

    private GroupedData(List<ResampledData> dataList) {
        this.dataList = dataList;
    }

    public static GroupedData groupDataByPoints(XYSeries xySeries, int... pointsPerGroups) {
        int size = pointsPerGroups.length == 0 ? 1 : pointsPerGroups.length;
        List<ResampledData> dataList = new ArrayList<>(size);
        for (int i = 0; i < pointsPerGroups.length; i++) {
            int points = pointsPerGroups[i];
            if (points > 1) {
                Resampler resampler = Resampler.createEqualPointsResampler(points);
                resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
                resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
                resampler.resampleAndAppend(xySeries.getDataTable());
                dataList.add(new ResampledData(xySeries, resampler, xySeries.getGroupingApproximationX(), xySeries.getGroupingApproximationY()));
            }
        }
        return new GroupedData(dataList);
    }


    public static GroupedData groupDataByIntervals(XYSeries xySeries, double... intervals) {
        int size = intervals.length == 0 ? 1 : intervals.length;
        List<ResampledData> dataList = new ArrayList<>(size);
        for (int i = 0; i < intervals.length; i++) {
            Resampler resampler = Resampler.createEqualIntervalResampler(intervals[i]);
            resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
            resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
            resampler.resampleAndAppend(xySeries.getDataTable());
            dataList.add(new ResampledData(xySeries, resampler, xySeries.getGroupingApproximationX(), xySeries.getGroupingApproximationY()));

        }
        return new GroupedData(dataList);
    }

    public static GroupedData groupDataByTimeIntervals(XYSeries xySeries, TimeInterval... timeIntervals) {
        int size = timeIntervals.length == 0 ? 1 : timeIntervals.length;
        List<ResampledData> dataList = new ArrayList<>(size);
        for (int i = 0; i < timeIntervals.length; i++) {
            Resampler resampler = Resampler.createEqualTimeIntervalResampler(timeIntervals[i]);
            resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
            resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
            resampler.resampleAndAppend(xySeries.getDataTable());
            dataList.add(new ResampledData(xySeries, resampler, xySeries.getGroupingApproximationX(), xySeries.getGroupingApproximationY()));
        }
        return new GroupedData(dataList);
    }

    public XYSeries getData(double xLength, int markSize) {
        if (dataList.size() == 1) {
            return dataList.get(0).getData();
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                XYSeries data = dataList.get(i).getData();
                int dataPoints = (int) (xLength / markSize) + 1;
                if (data.size() <= dataPoints) {
                    return dataList.get(i).getData();
                }
            }
            return dataList.get(dataList.size() - 1).getData();
        }
    }

    public void dataAppended() {
        for (ResampledData ResampledData : dataList) {
            ResampledData.dataAppended();
        }
    }

    
    static class ResampledData {
        private XYSeries originalData;
        private int dataSize;
        private Resampler resampler;
        private GroupingApproximation xApprox;
        private GroupingApproximation yApprox;

        public ResampledData(XYSeries originalData, Resampler r, GroupingApproximation xApprox, GroupingApproximation yApprox) {
            this.originalData = originalData;
            dataSize = originalData.size();
            resampler = r;
            this.xApprox = xApprox;
            this.yApprox = yApprox;
        }

        
        public XYSeries getData() {
            XYSeries xySeries = new XYSeries(resampler.resultantData());
            xySeries.setGroupingApproximationX(xApprox);
            xySeries.setGroupingApproximationY(yApprox);
            return xySeries;
        }
        
        public void appendData(XYSeries data) {
            resampler.resampleAndAppend(data.getDataTable());
        }
        
        public void dataAppended() {
            int from = dataSize;
            dataSize = originalData.size();
            int length = dataSize - from;
            resampler.resampleAndAppend(originalData.getDataTable(), from, length);
        }
    }
}
