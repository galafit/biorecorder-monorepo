package com.biorecorder.data.datatable;


public interface Column {
    String name();
    BaseType type();
    int size();
    double value(int index);
    String label(int index);
    double min();
    double max();
    void append(Column col) throws IllegalArgumentException;
    Column view(int from, int length);
    Column view(int[] order);
    Column emptyCopy();

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
