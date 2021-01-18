package com.biorecorder.data.frame.impl;

import com.biorecorder.data.frame.*;
import com.biorecorder.data.frame.Interval;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.sequence.IntSequence;
import com.biorecorder.data.sequence.IntSequence;
import com.biorecorder.data.utils.PrimitiveUtils;
import com.biorecorder.data.sequence.SequenceUtils;


/**************************************
 * This file is automatically created.
 * DO NOT MODIFY IT!
 * Edit template file _E_Column.tmpl
 *************************************/

class IntColumn implements Column {
    private IntSequence dataSequence;
    private StatsInt stats;

    public IntColumn(IntSequence data) {
        this.dataSequence = data;
    }

    public int intValue(int index) {
        return dataSequence.get(index);
    }


    @Override
    public int size() {
        return dataSequence.size();
    }

    @Override
    public double value(int index) {
        return dataSequence.get(index);
    }

    @Override
    public String label(int index) {
        return Integer.toString(dataSequence.get(index));
    }

    @Override
    public DataType dataType() {
        return DataType.Integer;
    }

    @Override
    public Column slice(int from, int length) {
        IntArrayList slicedData = new IntArrayList(length);
        for (int i = 0; i < length; i++) {
            slicedData.add(dataSequence.get(from + i));
        }
        return new IntColumn(slicedData);
    }

    @Override
    public Column slice(int from) {
        IntSequence slicedSequence = new IntSequence() {
            IntArrayList slicedData = new IntArrayList();
            @Override
            public int size() {
                return dataSequence.size() - from;
            }

            @Override
            public int get(int index) {
                if(index >= slicedData.size()) {
                    for (int i = slicedData.size(); i <= index; i++) {
                        slicedData.add(dataSequence.get(from + i));
                    }
                }
                return slicedData.get(index);
            }
        };
        return new IntColumn(slicedSequence);
    }

    @Override
    public Column view(int from) {
        IntSequence subSequence = new IntSequence() {
            @Override
            public int size() {
                return dataSequence.size() - from;
            }

            @Override
            public int get(int index) {
                return dataSequence.get(index + from);
            }
        };
        return new IntColumn(subSequence);
    }


    @Override
    public Column view(int from, int length) {
        IntSequence subSequence = new IntSequence() {
            @Override
            public int size() {
                return length;
            }

            @Override
            public int get(int index) {
                return dataSequence.get(index + from);
            }
        };
        return new IntColumn(subSequence);
    }

    @Override
    public Column view(int[] order) {
        IntSequence subSequence = new IntSequence() {
            @Override
            public int size() {
                return order.length;
            }

            @Override
            public int get(int index) {
                return dataSequence.get(order[index]);
            }
        };
        return new IntColumn(subSequence);
    }

    @Override
    public int bisect(double value, int from, int length) {
        return SequenceUtils.bisect(dataSequence, PrimitiveUtils.roundDouble2int(value), from, length);
    }

    @Override
    public int[] sort(int from, int length, boolean isParallel) {
        return SequenceUtils.sort(dataSequence, from, length, isParallel);
    }


    @Override
    public IntSequence group(double interval, DynamicSize length) {
        int intervalCasted = PrimitiveUtils.roundDouble2int(interval);
        if(intervalCasted == 0) {
            intervalCasted = PrimitiveUtils.intMinValue();
        }
        return group(new IntIntervalProvider(intervalCasted), length);
    }

    @Override
    public IntSequence group(TimeInterval timeInterval, DynamicSize length) {
        return group(new TimeIntervalProvider(timeInterval), length);
    }

    private IntSequence group(IntervalProvider intervalProvider, DynamicSize length) {
        IntSequence groupIndexes = new IntSequence() {
            IntArrayList groupIndexesList = new IntArrayList();
            @Override
            public int size() {
                update();
                return groupIndexesList.size();
            }

            @Override
            public int get(int index) {
                return groupIndexesList.get(index);
            }

            private void update() {
                int groupListSize = groupIndexesList.size();
                int l = length.size();
                if (l == 0 || (groupListSize > 0 && groupIndexesList.get(groupListSize - 1) == l)) {
                    return;
                }

                int from;
                if (groupListSize == 0) {
                    groupIndexesList.add(0);
                    from = 0;
                } else {
                    // delete last "closing" group
                    groupIndexesList.remove(groupListSize - 1);
                    from = groupIndexesList.get(groupListSize - 2);
                }

                Interval currentGroupInterval = intervalProvider.getContaining(dataSequence.get(from));
                for (int i = from + 1; i < l; i++) {
                    int data = dataSequence.get(i);
                    if (!currentGroupInterval.contains(data)) {
                        groupIndexesList.add(i);
                        currentGroupInterval = intervalProvider.getNext(); // main scenario
                        if(!currentGroupInterval.contains(data)) { // rare situation
                            currentGroupInterval = intervalProvider.getContaining(data);
                        }
                    }
                }
                // add last "closing" groupByEqualIntervals
                groupIndexesList.add(l);
            }
        };
        return groupIndexes;
    }

    /**
     * @return grouping function Object corresponding
     * to the given type of data (IntGroupingAvg, FloatGroupingMin and so on)
     */
    private IntAggFunction getAggFunction(Aggregation aggregation) {
        // Capitalize the first letter of dataType string
        String type = dataType().toString().substring(0, 1).toUpperCase() + dataType().toString().substring(1);
        String functionClassName = "com.biorecorder.data.frame.impl." + type + aggregation.toString();
        try {
            return (IntAggFunction) (Class.forName(functionClassName)).newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected int groupsCount(IntSequence groupIndexes, boolean isDataAppendMode) {
        int groupsCount = groupIndexes.size() - 1;
        if (isDataAppendMode && groupsCount > 0) {
            groupsCount--;
        }
        return groupsCount;
    }

    @Override
    public Column resample(Aggregation aggregation, IntSequence groupIndexes, boolean isDataAppendMode) {
        IntSequence resultantSequence = new IntSequence() {
            private IntAggFunction aggFunction = getAggFunction(aggregation);

            @Override
            public int size() {
                return groupsCount(groupIndexes, isDataAppendMode);
            }

            @Override
            public int get(int index) {
                aggFunction.reset();
                int n = aggFunction.getN();
                int length = groupIndexes.get(index + 1) - groupIndexes.get(index) - n;
                int from = groupIndexes.get(index) + n;
                if (length > 0) {
                    aggFunction.add(dataSequence, from, length);
                }
                return aggFunction.getValue();
            }
        };
        return new IntColumn(resultantSequence);
    }

    @Override
    public Column resample(Aggregation aggregation, int points, boolean isDataAppendMode) {
        return resample(aggregation, groupIndexes(points), isDataAppendMode);
    }

    protected IntSequence groupIndexes(int points) {
        return new IntSequence() {
            int size;

            @Override
            public int size() {
                int dataSize = dataSequence.size();
                if (dataSize % points == 0) {
                    size = dataSize / points + 1;
                } else {
                    size = dataSize / points + 2;
                }
                return size;
            }

            @Override
            public int get(int index) {
                if (index == size - 1) {
                    return dataSequence.size();
                } else {
                    return index * points;
                }
            }
        };
    }


    private StatsInt calculateStats(int from, int length) {
        int min1 = dataSequence.get(from);
        int max1 = min1;
        boolean isIncreasing1 = true;
        boolean isDecreasing1 = true;

        for (int i = 1; i < length; i++) {
            int data_i = dataSequence.get(i + from);
            min1 = (int)Math.min(min1, data_i);
            max1 = (int)Math.max(max1, data_i);
            if (isIncreasing1 || isDecreasing1) {
                int diff = (int)(data_i - dataSequence.get(i + from - 1));
                if (isDecreasing1 && diff > 0) {
                    isDecreasing1 = false;
                }
                if (isIncreasing1 && diff < 0) {
                    isIncreasing1 = false;
                }
            }
        }

        return new StatsInt(length, min1, max1, isIncreasing1, isDecreasing1);
    }

    @Override
    public Stats stats(int length) {
        if (length <= 0) {
            String errMsg = "Data size = "+ length + ". Statistic can be calculated only if size > 0";
            throw new IllegalStateException(errMsg);
        }
        if (length <= 2) {
            return calculateStats(0, length);
        }

        if (stats != null && length < stats.count()) {
            stats = null;
        }
        if (stats == null) {
            stats = calculateStats(0, length);
        }

        if (length > stats.count()) {
            StatsInt statsAdditional = calculateStats(stats.count(), length - stats.count());
            int min = (int)Math.min(stats.getMin(), statsAdditional.getMin());
            int max = (int)Math.max(stats.getMax(), statsAdditional.getMax());
            int diff = (int)(dataSequence.get(stats.count) - dataSequence.get(stats.count() - 1));
            boolean isIncreasing = stats.isIncreasing() && statsAdditional.isIncreasing() && diff >= 0;
            boolean isDecreasing = stats.isDecreasing() && statsAdditional.isDecreasing() && diff <= 0;
            stats = new StatsInt(length, min, max, isIncreasing, isDecreasing);
        }
        return stats;
    }

    class StatsInt implements Stats {
        private int count;
        private final int min;
        private final int max;
        private final boolean isIncreasing;
        private final boolean isDecreasing;

        public StatsInt(int count, int min, int max, boolean isIncreasing, boolean isDecreasing) {
            this.count = count;
            this.min = min;
            this.max = max;
            this.isIncreasing = isIncreasing;
            this.isDecreasing = isDecreasing;
        }

        int getMin() {
            return min;
        }

        int getMax() {
            return max;
        }

        int count() {
            return count;
        }

        @Override
        public double min() {
            return min;
        }

        @Override
        public double max() {
            return max;
        }

        @Override
        public boolean isIncreasing() {
            return isIncreasing;
        }

        @Override
        public boolean isDecreasing() {
            return isDecreasing;
        }
    }

    class IntIntervalProvider implements IntervalProvider {
        int interval;
        int currentIntervalStart;

        public IntIntervalProvider(int interval) {
            this.interval = interval;
            currentIntervalStart = 0;
        }

        @Override
        public Interval getContaining(double value) {
            int castedValue = PrimitiveUtils.roundDouble2int(value);
            currentIntervalStart = (int) (PrimitiveUtils.round(castedValue / interval) * interval);
            if (currentIntervalStart > value) {
                currentIntervalStart -= interval;
            }
            return new IntInterval(currentIntervalStart, (int)(currentIntervalStart + interval));

        }

        @Override
        public Interval getNext() {
            currentIntervalStart += interval;
            return new IntInterval(currentIntervalStart, (int)(currentIntervalStart + interval));

        }

        @Override
        public Interval getPrevious() {
            currentIntervalStart -= interval;
            return new IntInterval(currentIntervalStart, (int)(currentIntervalStart + interval));
        }
    }

    class IntInterval implements Interval {
        private final int start;
        private final int nextIntervalStart;

        public IntInterval(int start, int nextIntervalStart) {
            this.start = start;
            this.nextIntervalStart = nextIntervalStart;
        }

        //As we will use methods contains only on INCREASING data
        //we do only one check (value < nextIntervalStart) instead of both
        @Override
        public boolean contains(byte value) {
            // return value >= currentIntervalStart && value < nextIntervalStart;
            return value < nextIntervalStart;
        }

        @Override
        public boolean contains(short value) {
            return value < nextIntervalStart;
        }

        @Override
        public boolean contains(int value) {
            return value < nextIntervalStart;
        }

        @Override
        public boolean contains(long value) {
            return value < nextIntervalStart;
        }

        @Override
        public boolean contains(float value) {
            return value < nextIntervalStart;
        }

        @Override
        public boolean contains(double value) {
            return value < nextIntervalStart;
        }

    }
}
