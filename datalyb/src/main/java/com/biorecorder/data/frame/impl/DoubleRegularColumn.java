package com.biorecorder.data.frame.impl;


import com.biorecorder.data.frame.*;
import com.biorecorder.data.sequence.DoubleSequence;
import com.biorecorder.data.sequence.IntSequence;

/**
 * Created by galafit on 20/1/19.
 */
class DoubleRegularColumn extends DoubleColumn implements RegularColumn {
    private final boolean isLong;
    private final double startValue;
    private final double step;
    private int size;

    public DoubleRegularColumn(double startValue, double step, int size) {
        super(new DoubleSequence() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public double get(int index) {
                return startValue + step * index;
            }
        });
        this.startValue = startValue;
        this.step = step;
        this.size = size;
        if(startValue == (long)startValue && step == (long) step) {
            isLong = true;
        } else {
            isLong = false;
        }
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
    public String label(int index) {
        if(isLong) {
            return Long.toString((long) value(index));
        }
        return super.label(index);
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
        return new DoubleRegularColumn(value(from), step, length);
    }

    @Override
    public Column slice(int from) {
        return new DoubleRegularColumn(value(from), step, size - from);
    }


    @Override
    public Column view(int from) {
        return new DoubleRegularColumn(value(from), step, size - from);
    }

    @Override
    public Column view(int from, int length) {
        return new DoubleRegularColumn(value(from), step, length);
    }

    @Override
    public Column resample(Aggregation aggregation, int points,  boolean isDataAppendMode) throws IllegalArgumentException {
        int sizeNew = size/points;
        switch (aggregation) {
            case MIN:
            case FIRST: {
                double startNew = startValue;
                double stepNew = step * points;
                return new DoubleRegularColumn(startNew, stepNew, sizeNew);
            }
            case MAX:
            case LAST: {
                double startNew = startValue + step * points;
                double stepNew = step * points;
                return new DoubleRegularColumn(startNew, stepNew, sizeNew);
            }
            case COUNT: {
                double startNew = points;
                double stepNew = 0;
                return new DoubleRegularColumn(startNew, stepNew, sizeNew);
            }
            case SUM:{
                double startNew = sum(0, points);
                double stepNew = step * points * points;
                return new DoubleRegularColumn(startNew, stepNew, sizeNew);
            }
            case AVERAGE:{
                double startNew = avg(0, points);
                double stepNew = step * points;
                return new DoubleRegularColumn(startNew, stepNew, sizeNew);
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
                DoubleSequence resultantSequence = new DoubleSequence() {
                    @Override
                    public int size() {
                       return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public double get(int index) {
                        return value(groupIndexes.get(index));
                    }
                };
                return new DoubleColumn(resultantSequence);
            }
            case MAX:
            case LAST: {
                DoubleSequence resultantSequence = new DoubleSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public double get(int index) {
                        return value(groupIndexes.get(index + 1) - 1);
                    }
                };
                return new DoubleColumn(resultantSequence);
            }
            case COUNT: {
                DoubleSequence resultantSequence = new DoubleSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public double get(int index) {
                        return groupIndexes.get(index + 1) - groupIndexes.get(index);
                    }
                };
                return new DoubleColumn(resultantSequence);
            }
            case SUM: {
                DoubleSequence resultantSequence = new DoubleSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public double get(int index) {
                        return sum(groupIndexes.get(index), groupIndexes.get(index + 1) - groupIndexes.get(index));
                    }
                };
                return new DoubleColumn(resultantSequence);
            }
            case AVERAGE: {
                DoubleSequence resultantSequence = new DoubleSequence() {
                    @Override
                    public int size() {
                        return groupsCount(groupIndexes, isDataAppendMode);
                    }

                    @Override
                    public double get(int index) {
                        return avg(groupIndexes.get(index), groupIndexes.get(index + 1) - groupIndexes.get(index));
                    }
                };
                return new DoubleColumn(resultantSequence);
            }
            default:
                String errMsg = "Unsupported Aggregate function: "+aggregateFunction;
                throw new IllegalArgumentException(errMsg);
        }
    }

    private double sum(int from, int length) {
        return (value(from) + value(from + length - 1)) * length / 2;
    }

    private double avg(int from, int length) {
        return sum(from, length) / length;
    }

}
