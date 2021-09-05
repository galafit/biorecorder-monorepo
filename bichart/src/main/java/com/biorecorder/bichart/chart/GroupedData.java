package com.biorecorder.bichart.chart;

import com.biorecorder.bichart.XYData;
import com.biorecorder.bichart.configs.GroupingType;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.datatable.aggregation.Resampler;
import com.biorecorder.data.time.TimeInterval;

import java.util.ArrayList;
import java.util.List;

class GroupedData {
    List<DataWrapper> dataList = new ArrayList<>();

    public GroupedData(XYData xyData, GroupingType groupingType, double[] intervals, double xLength, int markSize) {
        if(xyData.size() <= 1) {
            dataList.add(new RawData(xyData));
            return;
        }
        if(intervals == null || intervals.length == 0) {
            intervals = new double[1];
            intervals[0] = bestInterval(xyData, xLength, markSize);
        }

        for (int i = 0; i < intervals.length; i++) {
            double interval = intervals[i];
            int pointsPerInterval = intervalToPoints(xyData, interval);
            if (pointsPerInterval > 1) {
                Resampler resampler;
                if (groupingType == GroupingType.EQUAL_POINTS) {
                    resampler = Resampler.createEqualPointsResampler(pointsPerInterval);
                } else {
                    resampler = Resampler.createEqualIntervalResampler(interval);
                }
                resampler.setColumnAggregations(0, xyData.getGroupingApproximationX().getAggregation());
                resampler.setColumnAggregations(1, xyData.getGroupingApproximationY().getAggregation());
                resampler.resampleAndAppend(xyData.getDataTable());
                dataList.add(new ResampledData(resampler));
            } else {
                dataList.add(new RawData(xyData));
            }
        }
    }

    public GroupedData(XYData xyData, GroupingType groupingType, TimeInterval[] timeIntervals, double xLength, int markSize) {
        if(xyData.size() <= 1) {
            dataList.add(new RawData(xyData));
            return;
        }

        if(timeIntervals == null || timeIntervals.length == 0) {
            timeIntervals = new TimeInterval[1];
            timeIntervals[0] = TimeInterval.getUpper(Math.round(bestInterval(xyData, xLength, markSize)), false);
        }
        for (int i = 0; i < timeIntervals.length; i++) {
            TimeInterval timeInterval = timeIntervals[i];
            int pointsPerInterval = intervalToPoints(xyData, timeInterval.toMilliseconds());
            if (pointsPerInterval > 1) {
                Resampler resampler;
                if (groupingType == GroupingType.EQUAL_POINTS) {
                    resampler = Resampler.createEqualPointsResampler(pointsPerInterval);
                } else {
                    resampler = Resampler.createEqualTimeIntervalResampler(timeInterval);
                }

                resampler.setColumnAggregations(0, xyData.getGroupingApproximationX().getAggregation());
                resampler.setColumnAggregations(1, xyData.getGroupingApproximationY().getAggregation());

                resampler.resampleAndAppend(xyData.getDataTable());
                dataList.add(new ResampledData(resampler));
            } else {
                dataList.add(new RawData(xyData));
            }
        }
    }

    public XYData getData(double xLength, int markSize) {
        if (dataList.size() == 1) {
            return dataList.get(0).getData();
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                XYData data = dataList.get(i).getData();
                int dataPoints = (int) (xLength / markSize) + 1;
                if (data.rowCount() <= dataPoints) {
                    return dataList.get(i).getData();
                }
            }
            return dataList.get(dataList.size() - 1).getData();
        }
    }

    public void appendData(XYData data) {
        for (DataWrapper dataWrapper : dataList) {
            dataWrapper.appendData(data);
        }
    }

    static int intervalToPoints(XYData data, double interval) {
        //double dataStep = dataRange(data).length() / (data.rowCount() - 1);
        //return (int)Math.round(interval / dataStep);
        double groups = dataRange(data).length() / interval;
        int pointsPerGroup = (int)Math.round(data.rowCount() / groups);
        return pointsPerGroup;
    }

    static double bestInterval(XYData data, double xLength, int markSize) {
        return dataRange(data).length() * markSize / xLength;
    }

    static int bestPointsInGroup(int dataSize, double xLength, int markSize) {
        return (int)Math.round(dataSize * markSize / xLength);
    }

    // suppose that data is ordered
    static Range dataRange(XYData data) {
        if(data != null && data.size() > 0) {
            return new Range(data.xValue(0), data.xValue(data.rowCount() - 1));
        }
        return null;
    }

    interface DataWrapper {
        XYData getData();

        void appendData(XYData data);
    }

    class RawData implements DataWrapper {
        private XYData data;

        public RawData(XYData data) {
            this.data = data;
        }

        @Override
        public XYData getData() {
            return data;
        }

        @Override
        public void appendData(XYData data) {
            data.appendData(data);
        }
    }

    class ResampledData implements DataWrapper {
        Resampler resampler;

        public ResampledData(Resampler r) {
            resampler = r;
        }

        @Override
        public XYData getData() {
            return new XYData(resampler.resultantData());
        }

        @Override
        public void appendData(XYData data) {
            resampler.resampleAndAppend(data.getDataTable());
        }
    }
}
