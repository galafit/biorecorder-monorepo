package com.biorecorder.bichart;

import com.biorecorder.datalyb.datatable.DataTable;
import com.biorecorder.datalyb.datatable.aggregation.Resampler;
import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.List;

class GroupedData {
    private List<Resampler> dataList;
    private int sampleCount;

    private GroupedData(List<Resampler> dataList, int sampleCount) {
        this.dataList = dataList;
        this.sampleCount = sampleCount;
    }

    public static GroupedData groupDataByPoints(XYSeries xySeries, int... pointsPerGroups) {
        int size = pointsPerGroups.length == 0 ? 1 : pointsPerGroups.length;
        List<Resampler> dataList = new ArrayList<>(size);
        for (int i = 0; i < pointsPerGroups.length; i++) {
            int points = pointsPerGroups[i];
            if (points > 1) {
                Resampler resampler = Resampler.createEqualPointsResampler(points);
                resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
                resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
                resampler.resampleAndAppend(xySeries.getDataTable());
                dataList.add(resampler);
            }
        }
        return new GroupedData(dataList, xySeries.size());
    }


    public static GroupedData groupDataByIntervals(XYSeries xySeries, double... intervals) {
        int size = intervals.length == 0 ? 1 : intervals.length;
        List<Resampler> dataList = new ArrayList<>(size);
        for (int i = 0; i < intervals.length; i++) {
            Resampler resampler = Resampler.createEqualIntervalResampler(intervals[i]);
            resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
            resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
            resampler.resampleAndAppend(xySeries.getDataTable());
            dataList.add(resampler);

        }
        return new GroupedData(dataList, xySeries.size());
    }

    public static GroupedData groupDataByTimeIntervals(XYSeries xySeries, TimeInterval... timeIntervals) {
        int size = timeIntervals.length == 0 ? 1 : timeIntervals.length;
        List<Resampler> dataList = new ArrayList<>(size);
        for (int i = 0; i < timeIntervals.length; i++) {
            Resampler resampler = Resampler.createEqualTimeIntervalResampler(timeIntervals[i]);
            resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
            resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
            resampler.resampleAndAppend(xySeries.getDataTable());
            dataList.add(resampler);
        }
        return new GroupedData(dataList, xySeries.size());
    }

    public XYSeries getData(double xLength, int markSize) {
        if (dataList.size() == 1) {
            Resampler resampler = dataList.get(0);
            return new XYSeries(resampler.resultantData());
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                DataTable data = dataList.get(i).resultantData();
                int dataPoints = (int) (xLength / markSize) + 1;
                if (data.rowCount() <= dataPoints) {
                    return new XYSeries(data);
                }
            }
            return new XYSeries(dataList.get(dataList.size() - 1).resultantData());
        }
    }

    public void appendData(XYSeries data, int from, int length) {
        for (Resampler resampler : dataList) {
            resampler.resampleAndAppend(data.getDataTable(), from, length);
        }
        sampleCount += length;
    }

    public int processedSampleCount() {
        return sampleCount;
    }
}
