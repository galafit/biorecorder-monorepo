package com.biorecorder.datalyb.datatable;

public interface Column {
    String name();
    BaseType type();
    int size();
    double value(int index);
    String label(int index);
    double[] minMax(int from, int length);

    // optional operation
    void clear() throws UnsupportedOperationException;
    /**
     * This method do not change the original column neither colToAppend!
     * But create a new column and copy in it data from both columns
     */
    Column append(int from, int length, Column colToAppend, int colToAppendFrom, int colToAppendLength) throws IllegalArgumentException;
    Column view(int from, int length);
    Column view(int[] order);
    Column emptyCopy();

    /**
     * Returns a sorted view of the underlying data without modifying the order
     * of the underlying data.
     * @return array of indexes representing sorted view of the underlying data
     */
    int[] sort(int from, int length, boolean isParallel);
    int bisect(double value, int from, int length);
    int bisectLeft(double value, int from, int length);
    int bisectRight(double value, int from, int length);

}