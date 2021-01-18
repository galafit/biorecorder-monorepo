package com.biorecorder.bichart.data;


import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame.TimeInterval;

/**
 * Simplified analogue of data table which
 * in fact is simply a collection of columns
 */
public interface ChartData {
    boolean isDataAppendMode();

    int rowCount();

    int columnCount();

    String getColumnName(int columnNumber);

    boolean isNumberColumn(int columnNumber);

    boolean isRegular();

    boolean isIncreasing();

    double value(int rowNumber, int columnNumber);

    String label(int rowNumber, int columnNumber);

    Range columnMinMax(int columnNumber);

    int bisect( double value, int[] sorter);

    ChartData view(int fromRowNumber, int length);

    ChartData view(int fromRowNumber);

    ChartData slice(int fromRowNumber, int length);

    ChartData slice(int fromRowNumber);

    ChartData concat(ChartData data);

    int[] sortedIndices(int sortColumn);

    void setColumnGroupApproximation(int columnNumber, GroupApproximation groupApproximation);

    GroupApproximation getColumnGroupApproximation(int columnNumber);

    ChartData resampleByEqualPointsNumber(int points);

    ChartData resampleByEqualInterval(int columnNumber, double interval);

    ChartData resampleByEqualTimeInterval(int columnNumber, TimeInterval timeInterval);

    void appendData();
}

