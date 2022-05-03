package com.biorecorder.bichart;

import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.time.TimeInterval;

public class TraceDataProcessor {
    private int minPointsForCrop = 10;
    private DataFeed dataFeed;
    private Trace trace;
    private boolean isDateTime;
    private boolean isFullGrouping;
    private GroupedData groupedData;
    private XYSeries traceData;
    private ProcessingConfig config;
    private boolean dataUpdated;
    private Scale scale;
    private int xLength;
    private double xMin;
    private double xMax;

    public TraceDataProcessor(DataFeed dataFeed, Trace trace) {
        this.dataFeed = dataFeed;
        this.trace = trace;
        dataFeed.addDataListener(new DataFeedListener() {
            @Override
            public void onStartFeeding(int numberOfDataPoints) {

            }

            @Override
            public void onStopFeeding() {

            }

            @Override
            public void onDataSeriesReceived(XYSeries data) {

            }
        });
    }

    public Range getDataRange() {
        return dataFeed.dataXRange();
    }

    public void onResize(int xLength) {
        this.xLength = xLength;
    }

    public void onXRangeChanged(double xMin, double xMax) {
        this.xMin = xMin;
        this.xMax = xMax;
    }


    private XYSeries cropData(XYSeries data, double min, double max) {
        if (data.size() < minPointsForCrop) {
            return data;
        }
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(data.size() - 1);
        if (dataMin > max || dataMax < min) {
            return data.getEmptyCopy();
        }
        if (dataMax == dataMin) {
            return data;
        }
        // crop data
        int indexFrom = 0;
        int indexTill = data.size();
        if (dataMin < min) {
            indexFrom = data.bisectLeft(min);
        }
        if (dataMax > max) {
            indexTill = data.bisectRight(max);
        }

        int extraPoints = config.getCropShoulder();
        indexFrom -= extraPoints;
        indexTill += extraPoints;
        if (indexFrom < 0) {
            indexFrom = 0;
        }
        if (indexTill > data.size()) {
            indexTill = data.size();
        }
        return data.view(indexFrom, indexTill - indexFrom);
    }

    private GroupedData groupData(XYSeries data, int markSize, double min, double max, int minMaxLength, double[] intervals, TimeInterval[] timeIntervals) {
        int dataSize = data.size();
        if(dataSize <= 1) {
            return null;
        }
        // suppose that data is ordered
        double dataMin = data.getX(0);
        double dataMax = data.getX(dataSize - 1);
        int dataLength = getDataLength(dataMin, dataMax, min, max, minMaxLength);
        if (config.getGroupingType() == GroupingType.EQUAL_INTERVALS) {
            if(isDateTime) {
                TimeInterval[] timeIntervals1 = normalizeTimeIntervals(timeIntervals, dataMin, dataMax, dataSize, dataLength, markSize);
                if (timeIntervals1 != null && timeIntervals1.length > 0) {
                    return GroupedData.groupDataByTimeIntervals(data, timeIntervals);
                }
            } else {
                double[] intervals1 = normalizeIntervals(intervals, dataMin, dataMax, dataSize, dataLength, markSize);
                if (intervals1 != null && intervals1.length > 0) {
                    return GroupedData.groupDataByIntervals(data, intervals);
                }
            }
        }

        if (config.getGroupingType() == GroupingType.EQUAL_POINTS) {
            IntArrayList pointsList = new IntArrayList();
            boolean intervalsNotNull = false;
            if(isDateTime) {
                if(timeIntervals !=null && timeIntervals.length > 0){
                    intervalsNotNull = true;
                    for (TimeInterval timeInterval : timeIntervals) {
                        int points = intervalToPoints(dataMin, dataMax, dataSize, timeInterval.toMilliseconds());
                        if(points > 1) {
                            pointsList.add(points);
                        }
                    }
                }
            } else {
                if(intervals != null && intervals.length > 0) {
                    intervalsNotNull = true;
                    for (double interval : intervals) {
                        int points = intervalToPoints(dataMin, dataMax, dataSize, interval);
                        if(points > 1) {
                            pointsList.add(points);
                        }
                    }
                }
            }

            if(intervalsNotNull == false) {
                int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
                if (bestPoints > 1) {
                    pointsList.add(bestPoints);
                }
            }
            if(pointsList.size() != 0) {
                return GroupedData.groupDataByPoints(data, pointsList.toArray());
            }
        }
        return null;
    }

    private int getDataLength(double dataMin,double dataMax,  double min, double max, int minMaxLength) {
        // prepare scale to calculate dataLength
        scale.setStartEnd(0, minMaxLength);
        scale.setMinMax(min, max);
        int dataLength = (int) (scale.scale(dataMax) - scale.scale(dataMin));
        if (dataLength < 1) {
            dataLength = 1;
        }
        return dataLength;
    }

    private double[] normalizeIntervals(double[] intervals, double dataMin, double dataMax, int dataSize, int dataLength, int markSize) {
        if (intervals != null && intervals.length != 0) {
            return intervals;
        }
        int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
        if (bestPoints > 1) {
            double bestInterval = bestGroupingInterval(dataMin, dataMax, dataLength, markSize);
            double[] bestIntervals = {bestInterval};
            return bestIntervals;
        }
        return null;
    }

    private TimeInterval[] normalizeTimeIntervals(TimeInterval[] timeIntervals, double dataMin, double dataMax, int dataSize, int dataLength, int markSize) {
        if (timeIntervals != null && timeIntervals.length != 0) {
            return timeIntervals;
        }
        int bestPoints = bestPointsInGroup(dataSize, dataLength, markSize);
        if (bestPoints > 1) {
            double bestInterval = bestGroupingInterval(dataMin, dataMax, dataLength, markSize);
            TimeInterval[] bestIntervals = {TimeInterval.getUpper((long) bestInterval, true)};
            return bestIntervals;
        }
        return null;
    }


    private int intervalToPoints(double dataMin, double dataMax, int dataSize, double interval) {
        int pointsPerGroup = (int) Math.round(interval *  dataSize / (dataMax - dataMin));
        return pointsPerGroup;
    }

    private int bestPointsInGroup(int dataSize, double dataLength, int markSize) {
        return (int) Math.round(dataSize * markSize / dataLength);
    }

    private double bestGroupingInterval(double dataMin, double dataMax, double dataLength, int markSize) {
        if (dataMax == dataMin) {
            return 1;
        }
        return (dataMax - dataMin) * markSize / dataLength;
    }
}
