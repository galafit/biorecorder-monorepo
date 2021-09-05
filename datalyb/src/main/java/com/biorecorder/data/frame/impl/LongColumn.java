package com.biorecorder.data.frame.impl;

import com.biorecorder.data.frame.*;
import com.biorecorder.data.frame.Interval;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.list.LongArrayList;
import com.biorecorder.data.sequence.IntSequence;
import com.biorecorder.data.sequence.LongSequence;
import com.biorecorder.data.utils.PrimitiveUtils;
import com.biorecorder.data.sequence.SequenceUtils;


/**************************************
 * This file is automatically created.
 * DO NOT MODIFY IT!
 * Edit template file _E_Column.tmpl
 *************************************/

class LongColumn implements Column {
    private LongSequence dataSequence;
    private StatsLong stats;

    public LongColumn(LongSequence data) {
        this.dataSequence = data;
    }

    public long longValue(int index) {
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
        return Long.toString(dataSequence.get(index));
    }

    @Override
    public DataType dataType() {
        return DataType.Long;
    }

    @Override
    public Column slice(int from, int length) {
        LongArrayList slicedData = new LongArrayList(length);
        for (int i = 0; i < length; i++) {
            slicedData.add(dataSequence.get(from + i));
        }
        return new LongColumn(slicedData);
    }

    @Override
    public Column slice(int from) {
        LongSequence slicedSequence = new LongSequence() {
            LongArrayList slicedData = new LongArrayList();
            @Override
            public int size() {
                return dataSequence.size() - from;
            }

            @Override
            public long get(int index) {
                if(index >= slicedData.size()) {
                    for (int i = slicedData.size(); i <= index; i++) {
                        slicedData.add(dataSequence.get(from + i));
                    }
                }
                return slicedData.get(index);
            }
        };
        return new LongColumn(slicedSequence);
    }

    @Override
    public Column view(int from) {
        LongSequence subSequence = new LongSequence() {
            @Override
            public int size() {
                return dataSequence.size() - from;
            }

            @Override
            public long get(int index) {
                return dataSequence.get(index + from);
            }
        };
        return new LongColumn(subSequence);
    }


    @Override
    public Column view(int from, int length) {
        LongSequence subSequence = new LongSequence() {
            @Override
            public int size() {
                return length;
            }

            @Override
            public long get(int index) {
                return dataSequence.get(index + from);
            }
        };
        return new LongColumn(subSequence);
    }

    @Override
    public Column view(int[] order) {
        LongSequence subSequence = new LongSequence() {
            @Override
            public int size() {
                return order.length;
            }

            @Override
            public long get(int index) {
                return dataSequence.get(order[index]);
            }
        };
        return new LongColumn(subSequence);
    }

    @Override
    public int bisect(double value, int from, int length) {
        return SequenceUtils.bisect(dataSequence, PrimitiveUtils.roundDouble2long(value), from, length);
    }

    @Override
    public int[] sort(int from, int length, boolean isParallel) {
        return SequenceUtils.sort(dataSequence, from, length, isParallel);
    }


    @Override
    public IntSequence group(double interval, DynamicSize length) {
        long intervalCasted = PrimitiveUtils.roundDouble2long(interval);
        if(intervalCasted == 0) {
            intervalCasted = PrimitiveUtils.longMinValue();
        }
        return group(new LongIntervalProvider(intervalCasted), length);
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
                    long data = dataSequence.get(i);
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
    private LongAggFunction getAggFunction(Aggregation aggregation) {
        // Capitalize the first letter of dataType string
        String type = dataType().toString().substring(0, 1).toUpperCase() + dataType().toString().substring(1);
        String functionClassName = "com.biorecorder.data.frame.impl." + type + aggregation.toString();
        try {
            return (LongAggFunction) (Class.forName(functionClassName)).newInstance();
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
        LongSequence resultantSequence = new LongSequence() {
            private LongAggFunction aggFunction = getAggFunction(aggregation);

            @Override
            public int size() {
                return groupsCount(groupIndexes, isDataAppendMode);
            }

            @Override
            public long get(int index) {
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
        return new LongColumn(resultantSequence);
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


    private StatsLong calculateStats(int from, int length) {
        long min1 = dataSequence.get(from);
        long max1 = min1;
        boolean isIncreasing1 = true;
        boolean isDecreasing1 = true;

        for (int i = 1; i < length; i++) {
            long data_i = dataSequence.get(i + from);
            min1 = (long)Math.min(min1, data_i);
            max1 = (long)Math.max(max1, data_i);
            if (isIncreasing1 || isDecreasing1) {
                long diff = (long)(data_i - dataSequence.get(i + from - 1));
                if (isDecreasing1 && diff > 0) {
                    isDecreasing1 = false;
                }
                if (isIncreasing1 && diff < 0) {
                    isIncreasing1 = false;
                }
            }
        }

        return new StatsLong(length, min1, max1, isIncreasing1, isDecreasing1);
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
            StatsLong statsAdditional = calculateStats(stats.count(), length - stats.count());
            long min = (long)Math.min(stats.getMin(), statsAdditional.getMin());
            long max = (long)Math.max(stats.getMax(), statsAdditional.getMax());
            long diff = (long)(dataSequence.get(stats.count) - dataSequence.get(stats.count() - 1));
            boolean isIncreasing = stats.isIncreasing() && statsAdditional.isIncreasing() && diff >= 0;
            boolean isDecreasing = stats.isDecreasing() && statsAdditional.isDecreasing() && diff <= 0;
            stats = new StatsLong(length, min, max, isIncreasing, isDecreasing);
        }
        return stats;
    }

    class StatsLong implements Stats {
        private int count;
        private final long min;
        private final long max;
        private final boolean isIncreasing;
        private final boolean isDecreasing;

        public StatsLong(int count, long min, long max, boolean isIncreasing, boolean isDecreasing) {
            this.count = count;
            this.min = min;
            this.max = max;
            this.isIncreasing = isIncreasing;
            this.isDecreasing = isDecreasing;
        }

        long getMin() {
            return min;
        }

        long getMax() {
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

    class LongIntervalProvider implements IntervalProvider {
        long interval;
        long currentIntervalStart;

        public LongIntervalProvider(long interval) {
            this.interval = interval;
            currentIntervalStart = 0;
        }

        @Override
        public Interval getContaining(double value) {
            long castedValue = PrimitiveUtils.roundDouble2long(value);
            currentIntervalStart = (long) (PrimitiveUtils.round(castedValue / interval) * interval);
            if (currentIntervalStart > value) {
                currentIntervalStart -= interval;
            }
            return new LongInterval(currentIntervalStart, (long)(currentIntervalStart + interval));

        }

        @Override
        public Interval getNext() {
            currentIntervalStart += interval;
            return new LongInterval(currentIntervalStart, (long)(currentIntervalStart + interval));

        }

        @Override
        public Interval getPrevious() {
            currentIntervalStart -= interval;
            return new LongInterval(currentIntervalStart, (long)(currentIntervalStart + interval));
        }
    }

    class LongInterval implements Interval {
        private final long start;
        private final long nextIntervalStart;

        public LongInterval(long start, long nextIntervalStart) {
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
