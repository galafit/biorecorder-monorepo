package com.biorecorder.data.frame;

import com.biorecorder.data.sequence.IntSequence;

/**
 * Created by galafit on 14/1/19.
 */
public interface Column {
    int size();

    double value(int index);

    String label(int index);

    DataType dataType();

    /**
     * Returns a sorted version of the column without modifying the order
     * of the underlying data.
     *
     * @return array of indexes representing sorted view of the underlying data
     */
    int[] sort(int from, int length, boolean isParallel);
    Column slice(int from);
    Column slice(int from, int length);
    Column view(int from);
    Column view(int from, int length);
    Column view(int[] order);

    int bisect(double value, int from, int length);

    Stats stats(int length);

    /**
     * Equal Width Binning. This method divides the entire range of column data (max - min)
     * into intervals of equal size, searches the intervalStart indexes for every such
     * unitMultiplier-bin-group and returns a sequence of that intervalStart indexes.
     * The column data must be sorted!!!
     */
    IntSequence group(double interval, DynamicSize length);

    IntSequence group(TimeInterval timeInterval, DynamicSize length);

    Column resample(Aggregation aggregation, IntSequence groupIndexes, boolean isDataAppendMode);
    Column resample(Aggregation aggregation, int points, boolean isDataAppendMode);

}
