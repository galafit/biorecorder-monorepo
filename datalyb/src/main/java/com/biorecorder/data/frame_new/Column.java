package com.biorecorder.data.frame_new;


public interface Column {
    String name();
    ColumnType type();
    int size();
    double value(int index);
    String label(int index);
    double min();
    double max();
    Column view(int from, int length);
    Column view(int[] order);
    Column append(Column col) throws UnsupportedOperationException, IllegalArgumentException;
    Column emptyCopy();
    boolean isNumberColumn();
    /**
     * Returns a sorted view of the underlying data without modifying the order
     * of the underlying data.
     * @return array of indexes representing sorted view of the underlying data
     */
    int[] sort(boolean isParallel);
    int bisect(double value);
    int bisectLeft(double value);
    int bisectRight(double value);
}
