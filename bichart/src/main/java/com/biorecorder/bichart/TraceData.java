package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame_new.aggregation.Resampler;
import com.biorecorder.data.frame_new.aggregation.TimeInterval;

import java.util.ArrayList;
import java.util.List;


class TraceData {
    List<DataWrapper> dataList;

    public TraceData(XYData xyData) {
        dataList = new ArrayList<>(1);
        dataList.add(new RawData(xyData));
    }

    public XYData getData(Range xMinMax, int xLength, int markSize) {
        if(dataList.size() == 1) {
            return dataList.get(0).getData();
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                XYData data = dataList.get(i).getData();
                int dataPoints = (int) Math.round(data.xMinMax().length() / xMinMax.length()) * xLength / markSize;
                if(data.rowCount() <= dataPoints) {
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

    public TraceData(XYData xyData, GroupingType groupingType, double[] intervals) {
        dataList = new ArrayList<>(intervals.length);
        for (int i = 0; i < intervals.length; i++) {
            double interval = intervals[i];
            double dataAvgStep = xyData.dataAvgStep();
            int dataPointsPerInterval = (int) Math.round(interval / dataAvgStep);
            if(dataPointsPerInterval > 1) {
                Resampler resampler;
                if(groupingType == GroupingType.EQUAL_POINTS) {
                    resampler = Resampler.createEqualPointsResampler(dataPointsPerInterval);
                } else {
                    resampler = Resampler.createEqualIntervalResampler(interval);
                }
                resampler.resampleAndAppend(xyData.getDataTable());
                dataList.add(new GroupedData(resampler));
            } else {
                dataList.add(new RawData(xyData));
            }
        }
    }

    public TraceData(XYData xyData, GroupingType groupingType, TimeInterval[] intervals) {
        dataList = new ArrayList<>(intervals.length);
        for (int i = 0; i < intervals.length; i++) {
            TimeInterval timeInterval = intervals[i];
            long interval = timeInterval.toMilliseconds();
            double dataAvgStep = xyData.dataAvgStep();
            int dataPointsPerInterval = (int) Math.round(interval / dataAvgStep);
            if(dataPointsPerInterval > 1) {
                Resampler resampler;
                if(groupingType == GroupingType.EQUAL_POINTS) {
                    resampler = Resampler.createEqualPointsResampler(dataPointsPerInterval);
                } else {
                    resampler = Resampler.createEqualTimeIntervalResampler(timeInterval);
                }
                resampler.resampleAndAppend(xyData.getDataTable());
                dataList.add(new GroupedData(resampler));
            } else {
                dataList.add(new RawData(xyData));
            }
        }
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

    class GroupedData implements DataWrapper {
       Resampler resampler;

        public GroupedData(Resampler r) {
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
