package com.biorecorder.bichart.dataprocessing;

import com.biorecorder.bichart.ChartData;
import com.biorecorder.bichart.GroupingApproximation;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame.TimeInterval;

/**
 * Simplified analogue of data table which
 * in fact is simply a collection of columns
 */
public interface ChartFrame extends ChartData {
    boolean isDataAppendMode();

    int rowCount();

    int columnCount();

    String columnName(int columnNumber);

    boolean isNumberColumn(int columnNumber);

    boolean isRegular();

    boolean isIncreasing();

    double value(int rowNumber, int columnNumber);

    String label(int rowNumber, int columnNumber);

    Range columnMinMax(int columnNumber);

    int bisect( double value, int[] sorter);

    int[] sortedIndices();

    ChartFrame view(int fromRowNumber, int length);

    ChartFrame view(int fromRowNumber);

    ChartFrame slice(int fromRowNumber, int length);

    ChartFrame slice(int fromRowNumber);

    ChartFrame concat(ChartFrame data);

    void setColumnGroupApproximation(int columnNumber, GroupingApproximation groupingApproximation);

    GroupingApproximation getColumnGroupApproximation(int columnNumber);

    ChartFrame resampleByEqualPointsNumber(int points);

    ChartFrame resampleByEqualInterval(int columnNumber, double interval);

    ChartFrame resampleByEqualTimeInterval(int columnNumber, TimeInterval timeInterval);

    void appendData();
}
