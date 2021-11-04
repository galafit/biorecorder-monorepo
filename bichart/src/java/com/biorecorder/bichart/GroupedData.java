package com.biorecorder.bichart;

import com.biorecorder.datalyb.datatable.aggregation.Resampler;
import com.biorecorder.datalyb.time.TimeInterval;

import java.util.ArrayList;
import java.util.List;

class GroupedData {
    List<DataWrapper> dataList = new ArrayList<>();

    public GroupedData(XYSeries xySeries, GroupingType groupingType, double[] intervals, double xLength, int markSize) {
        if(xySeries.size() <= 1) {
            dataList.add(new RawData(xySeries));
            return;
        }
        if(intervals == null || intervals.length == 0) {
            intervals = new double[1];
            intervals[0] = bestInterval(xySeries, xLength, markSize);
        }

        for (int i = 0; i < intervals.length; i++) {
            double interval = intervals[i];
            int pointsPerInterval = intervalToPoints(xySeries, interval);
            if (pointsPerInterval > 1) {
                Resampler resampler;
                if (groupingType == GroupingType.EQUAL_POINTS) {
                    resampler = Resampler.createEqualPointsResampler(pointsPerInterval);
                } else {
                    resampler = Resampler.createEqualIntervalResampler(interval);
                }
                resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
                resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
                resampler.resampleAndAppend(xySeries.getDataTable());
                dataList.add(new ResampledData(xySeries, resampler, xySeries.getGroupingApproximationX(), xySeries.getGroupingApproximationY()));
            } else {
                dataList.add(new RawData(xySeries));
            }
        }
    }

    public GroupedData(XYSeries xySeries, GroupingType groupingType, TimeInterval[] timeIntervals, double xLength, int markSize) {
        if(xySeries.size() <= 1) {
            dataList.add(new RawData(xySeries));
            return;
        }

        if(timeIntervals == null || timeIntervals.length == 0) {
            timeIntervals = new TimeInterval[1];
            timeIntervals[0] = TimeInterval.getUpper(Math.round(bestInterval(xySeries, xLength, markSize)), false);
        }
        for (int i = 0; i < timeIntervals.length; i++) {
            TimeInterval timeInterval = timeIntervals[i];
            int pointsPerInterval = intervalToPoints(xySeries, timeInterval.toMilliseconds());
            if (pointsPerInterval > 1) {
                Resampler resampler;
                if (groupingType == GroupingType.EQUAL_POINTS) {
                    resampler = Resampler.createEqualPointsResampler(pointsPerInterval);
                } else {
                    resampler = Resampler.createEqualTimeIntervalResampler(timeInterval);
                }
                resampler.setColumnAggregations(0, xySeries.getGroupingApproximationX().getAggregation());
                resampler.setColumnAggregations(1, xySeries.getGroupingApproximationY().getAggregation());
                resampler.resampleAndAppend(xySeries.getDataTable());
                dataList.add(new ResampledData(xySeries, resampler, xySeries.getGroupingApproximationX(), xySeries.getGroupingApproximationY()));
            } else {
                dataList.add(new RawData(xySeries));
            }
        }
    }

    public Range getDataRange() {
        return dataList.get(0).getDataRange();
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

    public void appendData(XYSeries data) {
        for (DataWrapper dataWrapper : dataList) {
            dataWrapper.appendData(data);
        }
    }

    public void dataAppended() {
        for (DataWrapper dataWrapper : dataList) {
            dataWrapper.dataAppended();
        }
    }

    static int intervalToPoints(XYSeries data, double interval) {
        //double dataStep = dataRange(data).length() / (data.rowCount() - 1);
        //return (int)Math.round(interval / dataStep);
        double groups = dataRange(data).length() / interval;
        int pointsPerGroup = (int)Math.round(data.size() / groups);
        return pointsPerGroup;
    }

    static double bestInterval(XYSeries data, double xLength, int markSize) {
        return dataRange(data).length() * markSize / xLength;
    }

    // suppose that data is ordered
    static Range dataRange(XYSeries data) {
        if(data.size() > 0) {
            return new Range(data.getX(0), data.getX(data.size() - 1));
        }
        return null;
    }

    static int bestPointsInGroup(int dataSize, double xLength, int markSize) {
        return (int)Math.round(dataSize * markSize / xLength);
    }

    interface DataWrapper {
        XYSeries getData();
        void appendData(XYSeries data);
        void dataAppended();
        Range getDataRange();
    }

    class RawData implements DataWrapper {
        private XYSeries data;

        public RawData(XYSeries data) {
            this.data = data;
        }

        @Override
        public XYSeries getData() {
            return data;
        }

        @Override
        public void appendData(XYSeries data) {
            data.appendData(data);
        }

        @Override
        public void dataAppended() {
            // do nothing
        }

        @Override
        public Range getDataRange() {
            return dataRange(data);
        }
    }

    class ResampledData implements DataWrapper {
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

        @Override
        public Range getDataRange() {
            return dataRange(new XYSeries(resampler.resultantData()));
        }

        @Override
        public XYSeries getData() {
            XYSeries xySeries = new XYSeries(resampler.resultantData());
            xySeries.setGroupingApproximationX(xApprox);
            xySeries.setGroupingApproximationY(yApprox);
            return xySeries;
        }

        @Override
        public void appendData(XYSeries data) {
            resampler.resampleAndAppend(data.getDataTable());
        }

        @Override
        public void dataAppended() {
            int from = dataSize;
            dataSize = originalData.size();
            int length = dataSize - from;
            resampler.resampleAndAppend(originalData.getDataTable(), from, length);
        }
    }
}
