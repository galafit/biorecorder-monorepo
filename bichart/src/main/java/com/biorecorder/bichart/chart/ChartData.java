package com.biorecorder.bichart.chart;


import com.biorecorder.bichart.graphics.Range;

/**
 * Simplified analogue of data table which
 * in fact is simply a collection of columns
 */
public interface ChartData {
    int rowCount();

    int columnCount();

    String columnName(int columnIndex);

    boolean isNumberColumn(int columnIndex);

    double value(int rowIndex, int columnIndex);

    String label(int rowIndex, int columnIndex);

    Range columnMinMax(int columnIndex);

    int bisect( double value, int[] sorter);

    int[] sortedIndices();

}
