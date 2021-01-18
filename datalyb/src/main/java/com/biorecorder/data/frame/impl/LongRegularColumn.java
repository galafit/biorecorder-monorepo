package com.biorecorder.data.frame.impl;

import com.biorecorder.data.frame.*;
import com.biorecorder.data.sequence.IntSequence;
import com.biorecorder.data.sequence.LongSequence;

/**
 * Created by galafit on 7/5/19.
 */
public class LongRegularColumn extends LongColumn implements RegularColumn{
    private long startValue;
    private long step;
    private int size;

    public LongRegularColumn(long startValue, long step, int size) {
        super(new LongSequence() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public long get(int index) {
                return startValue + step * index;
            }
        });
        this.startValue = startValue;
        this.step = step;
        this.size = size;
    }

    @Override
    public double start() {
        return startValue;
    }

    @Override
    public double step() {
        return step;
    }

    @Override
    public int bisect(double value, int from, int length) {
        int index = (int) ((value - value(0)) / step);
        if(value(index) != value) { //to maintain sorted order
            index++;
        }
        if(index < from) {
            return from;
        } else if(index > from + length) {
            index = from + length;
        }
        return index;
    }

    @Override
    public Stats stats(int length) {
        return new Stats() {
            @Override
            public double min() {
                return value(0);
            }

            @Override
            public double max() {
                return value(length - 1);
            }

            @Override
            public boolean isIncreasing() {
                return true;
            }

            @Override
            public boolean isDecreasing() {
                return false;
            }
        };
    }

    @Override
    public int[] sort(int from, int length, boolean isParallel) {
        int[] orderedIndexes = new int[length];
        for (int i = 0; i < length; i++) {
            orderedIndexes[i] = i + from;
        }
        return orderedIndexes;
    }

    @Override
    public Column slice(int from, int length) {
        return new LongRegularColumn(longValue(from), step, length);
    }

    @Override
    public Column slice(int from) {
        return new LongRegularColumn(longValue(from), step, size - from);
    }

    @Override
    public Column view(int from) {
        return new LongRegularColumn(longValue(from), step, size - from);
    }

    @Override
    public Column view(int from, int length) {
        return new LongRegularColumn(longValue(from), step, length);
    }

    @Override
    public Column resample(Aggregation aggregation, int points,  boolean isDataAppendMode) throws IllegalArgumentException {
        int sizeNew = size/points;
        switch (aggregation) {
            case MIN:
            case FIRST: {
                long startNew = startValue;
                long stepNew = step * points;
                return new LongRegularColumn(startNew, stepNew, sizeNew);
            }
            case MAX:
            case LAST: {
                long startNew = startValue + step * points;
                long stepNew = step * points;
                return new LongRegularColumn(startNew, stepNew, sizeNew);
            }
            case COUNT: {
                long startNew = points;
                long stepNew = 0;
                return new LongRegularColumn(startNew, stepNew, sizeNew);
            }
            case SUM:{
                long startNew = sum(0, points);
                long stepNew = step * points * points;
                return new LongRegularColumn(startNew, stepNew, sizeNew);
            }
            case AVERAGE:{
                long startNew = avg(0, points);
                long stepNew = step * points;
                return new LongRegularColumn(startNew, stepNew, sizeNew);
            }
            default:
                String errMsg = "Unsupported Aggregate function: "+ aggregation;
                throw new IllegalArgumentException(errMsg);
        }
    }

    @Override
    public Column resample(Aggregation aggregateFunction, IntSequence groupIndexes, boolean isDataAppendMode) throws IllegalArgumentException {
        switch (aggregateFunction) {
            case MIN:
            case FIRST: {
                LongSequence resultantSequence = new LongSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public long get(int index) {
                        return longValue(groupIndexes.get(index));
                    }
                };
                return new LongColumn(resultantSequence);
            }
            case MAX:
            case LAST: {
                LongSequence resultantSequence = new LongSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public long get(int index) {
                        return longValue(groupIndexes.get(index + 1) - 1);
                    }
                };
                return new LongColumn(resultantSequence);
            }
            case COUNT: {
                LongSequence resultantSequence = new LongSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public long get(int index) {
                        return groupIndexes.get(index + 1) - groupIndexes.get(index);
                    }
                };
                return new LongColumn(resultantSequence);
            }
            case SUM: {
                LongSequence resultantSequence = new LongSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public long get(int index) {
                        return sum(groupIndexes.get(index), groupIndexes.get(index + 1) - groupIndexes.get(index));
                    }
                };
                return new LongColumn(resultantSequence);
            }
            case AVERAGE: {
                LongSequence resultantSequence = new LongSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public long get(int index) {
                        return avg(groupIndexes.get(index), groupIndexes.get(index + 1) - groupIndexes.get(index));
                    }
                };
                return new LongColumn(resultantSequence);
            }
            default:
                String errMsg = "Unsupported Aggregate function: "+aggregateFunction;
                throw new IllegalArgumentException(errMsg);
        }
    }

    private long sum(int from, int length) {
        return (longValue(from) + longValue(from + length - 1)) * length / 2;
    }

    private long avg(int from, int length) {
        return sum(from, length) / length;
    }

}
