package com.biorecorder.bichart.dataprocessing;

import com.biorecorder.bichart.GroupingApproximation;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame.*;
import com.biorecorder.data.sequence.*;

import java.util.List;

/**
 * Created by galafit on 21/1/19.
 */
public class XYDataOld implements ChartFrame {
    private DataFrame dataFrame;
    private String xColumnName = "x";

    private XYDataOld(DataFrame dataFrame) {
        this.dataFrame = dataFrame;
    }

    public XYDataOld(double xStart, double xStep, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xStart, xStep);
    }

    public XYDataOld(double xStart, double xStep, int length) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xStart, xStep);
    }

    public XYDataOld(List<String> xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(ShortSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);

    }

    public XYDataOld(short[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(IntSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(int[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(LongSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(long[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(FloatSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(float[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(DoubleSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYDataOld(double[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public void addYColumn(String name, Function function) {
        dataFrame.addColumn(name, function, 0);
    }

    public void addYColumn(String name, ShortSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, short[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, IntSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, int[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, LongSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, long[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, FloatSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, float[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, DoubleSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, double[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void setColumnName(int columnNumber, String columnName) {
        dataFrame.setColumnName(columnNumber, columnName);
    }

    private GroupingApproximation aggregationsToAproximation(Aggregation[] aggregations) throws IllegalArgumentException {
        switch (aggregations.length) {
            case 0: {
                return null;
            }
            case 1: {
                switch (aggregations[0]) {
                    case FIRST:
                        return GroupingApproximation.OPEN;
                    case LAST:
                        return GroupingApproximation.CLOSE;
                    case MIN:
                        return GroupingApproximation.LOW;
                    case MAX:
                        return GroupingApproximation.HIGH;
                    case AVERAGE:
                        return GroupingApproximation.AVERAGE;
                    case SUM:
                        return GroupingApproximation.SUM;
                }
                break;
            }
            case 2: {
                if(aggregations[0] == Aggregation.MIN
                        && aggregations[1]  == Aggregation.MAX) {
                    return GroupingApproximation.RANGE;
                }
                break;
            }
            case 4: {
                if(aggregations[0] == Aggregation.FIRST
                        && aggregations[1]  == Aggregation.MAX
                        && aggregations[2]  == Aggregation.MIN
                        && aggregations[3]  == Aggregation.LAST) {
                    return GroupingApproximation.OHLC;
                }
                break;
            }
        }
        StringBuilder errMsg = new StringBuilder("Unsupported Aggregations: ");
        for (Aggregation agg : aggregations) {
            errMsg.append("  " + agg);
        }
        throw new IllegalArgumentException(errMsg.toString());
    }

    private Aggregation[] aproximationToAggregations(GroupingApproximation approximation) throws IllegalArgumentException {
        if(approximation == null) {
            return new Aggregation[0];
        }
        switch (approximation) {
            case SUM: {
                Aggregation[] aggregations = {Aggregation.SUM};
                return aggregations;
            }
            case OPEN: {
                Aggregation[] aggregations = {Aggregation.FIRST};
                return aggregations;
            }
            case CLOSE: {
                Aggregation[] aggregations = {Aggregation.LAST};
                return aggregations;
            }
            case AVERAGE: {
                Aggregation[] aggregations = {Aggregation.AVERAGE};
                return aggregations;
            }
            case HIGH: {
                Aggregation[] aggregations = {Aggregation.MAX};
                return aggregations;
            }
            case LOW: {
                Aggregation[] aggregations = {Aggregation.MIN};
                return aggregations;
            }
            case RANGE: {
                Aggregation[] aggregations = {Aggregation.MIN, Aggregation.MAX};
                return aggregations;
            }
            case OHLC: {
                Aggregation[] aggregations = {Aggregation.FIRST, Aggregation.MAX, Aggregation.MIN, Aggregation.LAST};
                return aggregations;
            }
        }
        String errMsg = "Unsupported approximation: " + approximation;
        throw new IllegalArgumentException(errMsg.toString());

    }


    @Override
    public void setColumnGroupApproximation(int columnNumber, GroupingApproximation groupingApproximation) {
        dataFrame.setColumnAggFunctions(columnNumber, aproximationToAggregations(groupingApproximation));
    }

    @Override
    public GroupingApproximation getColumnGroupApproximation(int columnNumber) {
        return aggregationsToAproximation(dataFrame.getColumnAggFunctions(columnNumber));
    }

    @Override
    public boolean isDataAppendMode() {
        return dataFrame.isDataAppendMode();
    }

    @Override
    public int rowCount() {
        return dataFrame.rowCount();
    }

    @Override
    public int columnCount() {
        return dataFrame.columnCount();
    }

    @Override
    public String columnName(int columnNumber) {
        return dataFrame.getColumnName(columnNumber);
    }

    @Override
    public boolean isNumberColumn(int columnNumber) {
        return dataFrame.columnType(columnNumber).isNumber();
    }

    @Override
    public boolean isRegular() {
        return dataFrame.isColumnRegular(0);
    }

    @Override
    public boolean isIncreasing() {
        Stats stats = dataFrame.stats(0);
        if (stats == null) {
            return true;
        }
        return dataFrame.stats(0).isIncreasing();
    }

    @Override
    public int[] sortedIndices() {
        return dataFrame.sortedIndices(0);
    }

    @Override
    public double value(int rowNumber, int columnNumber) {
        return dataFrame.value(rowNumber, columnNumber);
    }

    @Override
    public String label(int rowNumber, int columnNumber) {
        return dataFrame.label(rowNumber, columnNumber);
    }

    @Override
    public Range columnMinMax(int columnNumber) {
        Stats stats = dataFrame.stats(columnNumber);
        if (stats == null) {
            return null;
        }
        return new Range(stats.min(), stats.max());
    }

    @Override
    public int bisect(double value, int[] sorter) {
        return dataFrame.bisect(0, value, sorter);
    }

    @Override
    public ChartFrame slice(int fromRowNumber, int length) {
        return new XYDataOld(dataFrame.slice(fromRowNumber, length));
    }

    @Override
    public ChartFrame slice(int fromRowNumber) {
        return new XYDataOld(dataFrame.slice(fromRowNumber));
    }


    @Override
    public ChartFrame concat(ChartFrame data) {
        if(data instanceof XYDataOld) {
            return new XYDataOld(dataFrame.concat(((XYDataOld)data).dataFrame));
        }
        throw new IllegalArgumentException("XYData can be concatenated only with XYData");
    }

    @Override
    public ChartFrame view(int fromRowNumber) {
        return new XYDataOld(dataFrame.view(fromRowNumber));
    }


    @Override
    public ChartFrame view(int fromRowNumber, int length) {
        return new XYDataOld(dataFrame.view(fromRowNumber, length));
    }

    @Override
    public ChartFrame resampleByEqualPointsNumber(int points) {
        return new XYDataOld(dataFrame.resampleByEqualPointsNumber(points, true));
    }

    @Override
    public ChartFrame resampleByEqualInterval(int columnNumber, double interval) {
        return new XYDataOld(dataFrame.resampleByEqualInterval(columnNumber, interval, true));
    }

    @Override
    public ChartFrame resampleByEqualTimeInterval(int columnNumber, TimeInterval timeInterval) {
        return new XYDataOld(dataFrame.resampleByEqualTimeInterval(columnNumber, timeInterval, true));
    }



    @Override
    public void appendData() {
        dataFrame.appendData();
    }

}
