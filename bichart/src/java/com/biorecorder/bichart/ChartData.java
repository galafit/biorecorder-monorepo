package com.biorecorder.bichart;


import com.biorecorder.bichart.graphics.Range;

/**
 * Simplified analogue of data table which
 * in fact is simply a collection of columns
 */
public interface ChartData {
    int size();

    int columnCount();

    String columnName(int columnIndex);

    boolean isNumberColumn(int columnIndex);

    double value(int rowIndex, int columnIndex);

    String label(int rowIndex, int columnIndex);

    Range columnMinMax(int columnIndex);

    int bisect(int columnNumber, double value, int[] sorter);

    int[] sortedIndices(int columnNumber);
}

